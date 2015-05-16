package hu.ait.android.aloke.aloah.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Aloke on 5/10/15.
 */
public class WelcomeDialogFragment extends DialogFragment {


    public static final String TAG = "TAG";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Dialog dialog = new Dialog(getActivity());
        dialog.setTitle("Welcome to Aloah!");
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }
}
