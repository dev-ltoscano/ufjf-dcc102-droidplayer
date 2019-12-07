package br.com.ltoscano.droidplayer.app.helper.permission;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import java.util.Iterator;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.helper.dialog.DialogHelper;

public class PermissionHelper
{
    public static boolean isPermissionGranted(Context ctx, String permission)
    {
        return (ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestPermission(Activity activity, String[] permissions, int requestCode)
    {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static void requestPermissions(Context ctx, final int requestCode, List<String> permissionList, boolean essentialPermissions)
    {
        final Activity activity = (Activity)ctx;
        final List<String> notGrantedPermissionList = filterPermissionsNotGranted(ctx, permissionList);

        if(notGrantedPermissionList.isEmpty())
        {
            return;
        }

        boolean showInfoDialog = false;

        for(String permission : notGrantedPermissionList)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            {
                showInfoDialog = true;
                break;
            }
        }

        if (showInfoDialog && essentialPermissions)
        {
            DialogHelper.showAlertDialogWithPositiveAndNegativeButton(
                    ctx,
                    R.style.alert_dialog_style,
                    R.string.request_permissions_again,
                    R.string.yes,
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            requestPermission(activity, notGrantedPermissionList.toArray(new String[0]), requestCode);
                        }
                    },
                    R.string.no,
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            showEssentialPermissionNotGrantedDialog(activity);
                        }
                    },
                    new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            showEssentialPermissionNotGrantedDialog(activity);
                        }
                    });
        }
        else
        {
            requestPermission(activity, notGrantedPermissionList.toArray(new String[0]), requestCode);
        }
    }

    public static void showEssentialPermissionNotGrantedDialog(final Activity activity)
    {
        DialogHelper.showAlertDialogWithPositiveButton(
                activity,
                R.style.alert_dialog_style,
                R.string.essential_permissions_not_granted,
                R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        activity.finishAndRemoveTask();
                    }
                },
                new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialogInterface)
                    {
                        activity.finishAndRemoveTask();
                    }
                });
    }

    public static List<String> filterPermissionsNotGranted(Context ctx, List<String> permissionList)
    {
        Iterator<String> listIt = permissionList.iterator();

        while(listIt.hasNext())
        {
            String permission = listIt.next();

            if(isPermissionGranted(ctx, permission))
            {
                listIt.remove();
            }
        }

        return permissionList;
    }
}
