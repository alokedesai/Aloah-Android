package hu.ait.android.aloke.aloah;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by noah on 4/17/15.
 */
public class KeyEntryDialog extends DialogFragment{

    public static final String TAG = "KeyEntryDialog";


    public interface KeyEntryDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String key);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    KeyEntryDialogListener listener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (KeyEntryDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement KeyEntryDialogListener");
        }


    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Input your key");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_key_entry, null);
        builder.setView(v);

        final EditText etKey = (EditText) v.findViewById(R.id.etKey);


        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogPositiveClick(KeyEntryDialog.this,
                        etKey.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogNegativeClick(KeyEntryDialog.this);
            }
        });

        return builder.create();
    }
}
