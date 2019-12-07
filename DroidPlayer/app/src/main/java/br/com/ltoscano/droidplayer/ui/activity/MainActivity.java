package br.com.ltoscano.droidplayer.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.exception.FileSystemException;
import br.com.ltoscano.droidplayer.filesystem.FileSystem;
import br.com.ltoscano.droidplayer.filesystem.task.DownloadAsyncTask;
import br.com.ltoscano.droidplayer.filesystem.task.LoadMediaFilesAsyncTask;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.app.helper.permission.PermissionHelper;
import br.com.ltoscano.droidplayer.filesystem.task.StreamAsyncTask;
import br.com.ltoscano.droidplayer.filesystem.task.SynchronizeAsyncTask;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;
import br.com.ltoscano.droidplayer.network.service.NetworkService;
import br.com.ltoscano.droidplayer.ui.fragment.player.PlayerFragment;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_CODE_FOR_NONESSENTIAL_PERMISSIONS = 0;
    private final int REQUEST_CODE_FOR_ESSENTIAL_PERMISSIONS = 1;

    private void initializeServices()
    {
        startService(new Intent(this, NetworkService.class));
    }

    private void initializeFileSystem()
    {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);

        if(preferences.getBoolean("FirstRun", true))
        {
            FileSystem.createInstance(getFilesDir().getPath().toString());
        }
        else
        {
            try
            {
                FileSystem.loadInstance(getFilesDir().getPath().toString() + "/fs/droidplayer_fs.json");
            }
            catch (FileSystemException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
        }
    }

    private void requestPermissions()
    {
        Log.d(AppLogger.TAG, "Requesting permissions...");

        List<String> essentialPermissionList = new ArrayList<>();
        essentialPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        essentialPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        essentialPermissionList.add(Manifest.permission.READ_PHONE_STATE);
        essentialPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        essentialPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> nonessentialPermissionList = new ArrayList<>();
        nonessentialPermissionList.add(Manifest.permission.RECORD_AUDIO);

        PermissionHelper.requestPermissions(this, REQUEST_CODE_FOR_ESSENTIAL_PERMISSIONS, essentialPermissionList, true);
        PermissionHelper.requestPermissions(this, REQUEST_CODE_FOR_NONESSENTIAL_PERMISSIONS, nonessentialPermissionList, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_devices).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        requestPermissions();

        initializeServices();
        initializeFileSystem();

        boolean openInDevices = getIntent().getBooleanExtra("openInDevices", false);

        if(openInDevices)
        {
            navController.navigate(R.id.navigation_devices);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.available_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_load_music:
            {
                LoadMediaFilesAsyncTask loadMediaFilesAsyncTask = new LoadMediaFilesAsyncTask(this);
                loadMediaFilesAsyncTask.execute();

                return true;
            }
            case R.id.menu_sync:
            {
                SynchronizeAsyncTask synchronizeAsyncTask = new SynchronizeAsyncTask(this);
                synchronizeAsyncTask.execute();

                return true;
            }
            case R.id.download_music_menu:
            {
                AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

                int itemIndex = contextMenuInfo.position;
                MediaInfo mediaInfo = PlayerFragment.getInstance().getAvailableMediaList().get(itemIndex);

                DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask(mediaInfo);
                downloadAsyncTask.execute();

                return true;
            }
            case R.id.stream_music_menu:
            {
                AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

                int itemIndex = contextMenuInfo.position;
                MediaInfo mediaInfo = PlayerFragment.getInstance().getAvailableMediaList().get(itemIndex);

                StreamAsyncTask streamAsyncTask = new StreamAsyncTask(mediaInfo);
                streamAsyncTask.execute();

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_FOR_ESSENTIAL_PERMISSIONS:
            {
                for(int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d(AppLogger.TAG, "Permission '" + permissions[i] + "' granted");
                    }
                    else
                    {
                        Log.d(AppLogger.TAG, "Permission '" + permissions[i] + "' not granted");
                        PermissionHelper.showEssentialPermissionNotGrantedDialog(this);
                    }
                }

                break;
            }
            case REQUEST_CODE_FOR_NONESSENTIAL_PERMISSIONS:
            {
                for(int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d(AppLogger.TAG, "Permission '" + permissions[i] + "' granted");
                    }
                    else
                    {
                        Log.d(AppLogger.TAG, "Permission '" + permissions[i] + "' not granted");
                    }
                }

                break;
            }
        }
    }
}
