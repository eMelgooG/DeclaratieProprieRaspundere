package com.lea.Declaratie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;


public class SignatureViewDemoDialog extends DialogFragment {
    SignatureView signatureView;

    public static SignatureViewDemoDialog newInstance(Fragment caller) {

        SignatureViewDemoDialog demoDialog = new SignatureViewDemoDialog();
        demoDialog.setTargetFragment(caller, 0);

        return demoDialog;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
                int orientation = getActivity().getResources().getConfiguration().orientation;
                if(orientation!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

        signatureView = new SignatureView(context, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(signatureView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).updateImageView(signatureView.getContentDataBMP());
                    }
                })
                .setNeutralButton("Șterge", null);

        return builder.create();
    }

    @Override
    public void onCreate(Bundle icicle) {
        setCancelable(true);
        setRetainInstance(true);
        super.onCreate(icicle);
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureView.clearCanvas();
            }
        });
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onDestroyView();
    }
}
