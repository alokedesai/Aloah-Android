package hu.ait.android.aloke.aloah;

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

import hu.ait.android.aloke.aloah.crypto.CryptoUtils;

/**
 * Created by Aloke on 5/8/15.
 */
public class UploadEncryptedKey extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private String publicKey;
    private ParseObject user;
    private ProgressDialog progressDialog;

    public UploadEncryptedKey(Context context, ParseObject user) {
        this.context = context;
        this.user = user;

        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Authorizing user");
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
            Toast.makeText(context, "Key has been uploaded", Toast.LENGTH_LONG).show();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }


        }

    }


//    private void saveEncryptedKeyToSharedPreferences(CloudBlobClient blobClient) throws StorageException, IOException, URISyntaxException {
//        File tempFile = downloadTempKeyFile(blobClient);
//
//        BufferedReader brTest = new BufferedReader(new FileReader(tempFile));
//        String firstLine = brTest.readLine();
//        System.out.println("First line from load images: " + firstLine);
//
//        ((MainActivity) context).saveToSharedPreferences(CryptoUtils.ENCRYPTED_KEY, firstLine);
//
//        tempFile.delete();
//    }
//
//    private File downloadTempKeyFile(CloudBlobClient blobClient) throws URISyntaxException, StorageException, IOException {
//        CloudBlobContainer keyContainer = blobClient.getContainerReference("keycontainer");
//        CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(context.getString(R.string.user_id) + ".txt");
//        File tempKeyFile = File.createTempFile("tempkeyfile_download", ".tmp", context.getCacheDir());
//        System.out.println("the path of the file is: " + tempKeyFile.getAbsolutePath());
//        blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());
//        return tempKeyFile;
//    }

//    private String getUserId() {
//        SharedPreferences sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE);
//        return sp.getString("USER_ID", "-1");
//    }
}
