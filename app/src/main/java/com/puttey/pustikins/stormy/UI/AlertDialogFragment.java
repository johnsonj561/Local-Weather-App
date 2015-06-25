package com.puttey.pustikins.stormy.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.puttey.pustikins.stormy.R;

/**
 * Created by Pustikins on 6/8/2015.
 */
public class AlertDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.error_title))
            .setMessage(context.getString(R.string.error_message))
            .setPositiveButton(context.getString(R.string.error_message_button), null);
        AlertDialog dialog = builder.create();
        return dialog;

    }
}
