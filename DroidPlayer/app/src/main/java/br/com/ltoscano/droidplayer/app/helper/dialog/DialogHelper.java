package br.com.ltoscano.droidplayer.app.helper.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;

public class DialogHelper
{
    public static void showAlertDialogWithPositiveButton(
            Context ctx,
            int style,
            int message,
            int positiveButtonText,
            DialogInterface.OnClickListener onClickListenerPositive,
            DialogInterface.OnCancelListener onCancelListener)
    {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx, style);

        dialogBuilder
                .setMessage(message)
                .setPositiveButton(positiveButtonText, onClickListenerPositive)
                .setOnCancelListener(onCancelListener);

        dialogBuilder.create().show();
    }

    public static void showAlertDialogWithPositiveAndNegativeButton(
            Context ctx,
            int dialogStyle,
            int dialogMsg,
            int positiveButton,
            DialogInterface.OnClickListener positiveClickListener,
            int negativeButton,
            DialogInterface.OnClickListener negativeClickListener,
            DialogInterface.OnCancelListener cancelListener)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, dialogStyle);

        builder.setMessage(dialogMsg)
                .setPositiveButton(positiveButton, positiveClickListener)
                .setNegativeButton(negativeButton, negativeClickListener);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnCancelListener(cancelListener);
        alertDialog.show();
    }

    public static void showSnackBar(Context context, int actionMsg, int actionButton, View.OnClickListener actionListener, int duration)
    {
        View rootView = ((Activity)context).findViewById(android.R.id.content);

        final Snackbar snackbar = Snackbar.make(rootView, actionMsg, duration);

        if(actionListener != null)
        {
            snackbar.setAction(actionButton, actionListener);
        }
        else
        {
            snackbar.setAction(actionButton, new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    snackbar.dismiss();
                }
            });
        }


        snackbar.show();
    }
}
