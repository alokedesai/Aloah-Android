package hu.ait.android.aloke.aloah.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.AdminActivity;
import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.crypto.CryptoUtils;
import hu.ait.android.aloke.aloah.event.UploadEncryptedKeyEvent;

/**
 * Created by Aloke on 5/8/15.
 */
public class UploadEncryptedKey extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private String publicKey;
    private ParseObject user;
    private ProgressDialog progressDialog;
    private int index;

    public UploadEncryptedKey(Context context, ParseObject user, int index) {
        this.context = context;
        this.user = user;
        this.index = index;

        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage(context.getString(R.string.authorizing_user_progress_dialog_text));
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        publicKey = params[0];

        PrintStream printStream = null;

        boolean success = false;
        try {

            // derive the encrypted Key
            byte[] encryptedKey = CryptoUtils.encryptSymmetricKeyWithPublicKey(publicKey);
            String encryptedKeyAsString = Base64.encodeToString(encryptedKey, Base64.DEFAULT).replaceAll("\n", "");

            File outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), user.getObjectId() +".txt");
            printStream = new PrintStream(new FileOutputStream(outputFile));
            printStream.print(encryptedKeyAsString);

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((AdminActivity) context).getStorageAccount().createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(MainActivity.KEY_CONTAINER);
            CloudBlockBlob blob = container.getBlockBlobReference(outputFile.getName());
            blob.upload(new java.io.FileInputStream(outputFile), outputFile.length());

            //set user to be approved
            user.put("approved", true);
            user.save();

            success = true;
        } catch (IOException | StorageException | URISyntaxException | ParseException e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            progressDialog.dismiss();
        }
        EventBus.getDefault().post(new UploadEncryptedKeyEvent(index, success));
    }
}
