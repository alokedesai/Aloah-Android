package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import hu.ait.android.aloke.aloah.crypto.CryptoUtils;

/**
 * Created by Aloke on 5/14/15.
 */
public class CreateRSAKeys extends AsyncTask<Void, Void, KeyPair> {
    private Context context;
    private ParseObject currentUser;

    public CreateRSAKeys(Context context, ParseObject currentUser) {
        this.context = context;
        this.currentUser = currentUser;
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
        // saved to shared preferences
        if (keyPair != null) {
            CryptoUtils.saveRSAKeysToSharedPreferences(keyPair.getPublic(), keyPair.getPrivate());
        }
        ((MainActivity) context).makeToast("RSA Keys successfully created!");
    }
}
