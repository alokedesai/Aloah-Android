package hu.ait.android.aloke.aloah.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.event.CreateRSAKeysEvent;

/**
 * Created by Aloke on 5/14/15.
 */
public class CreateRSAKeys extends AsyncTask<Void, Void, KeyPair> {
    private Context context;
    private ParseObject currentUser;
    private ProgressDialog progressDialog;

    public CreateRSAKeys(Context context, ParseObject currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage(context.getString(R.string.creating_rsa_keys_progress_dialog_message));
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected KeyPair doInBackground(Void... params) {
        KeyPair pair = null;
        try {
            pair = CryptoUtils.generateRSAKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        String pubKey = Base64.encodeToString(pair.getPublic().getEncoded(), Base64.DEFAULT);

        currentUser.put("publicKey", pubKey);

        try {
            currentUser.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return pair;
    }

    @Override
    protected void onPostExecute(KeyPair keyPair) {
        progressDialog.dismiss();

        if (keyPair != null) {
            EventBus.getDefault().post(
                    new CreateRSAKeysEvent(keyPair));
        }
    }
}
