package com.bakatrouble.ifmo_timetable;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class FragmentHtmlDialog extends DialogFragment {
    public FragmentHtmlDialog(){
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setCancelable(true);
        builder.setNegativeButton("ОК",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        View layout = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog, null);

        TextView textView = (TextView)layout.findViewById(R.id.text_view);
        textView.setText(getArguments().getCharSequence("html"));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(layout);
        return builder.create();
    }
}
