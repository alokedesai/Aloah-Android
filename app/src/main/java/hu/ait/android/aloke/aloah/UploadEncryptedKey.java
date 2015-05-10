package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

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
    private String encryptedKey;

    public UploadEncryptedKey(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        encryptedKey = params[0];

        PrintStream printStream = null;
        try {
            File outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), context.getString(R.string.user_id) +".txt");
            printStream = new PrintStream(new FileOutputStream(outputFile));
            printStream.print(encryptedKey);

            // create client to connect to the azure sever
            CloudBlobClient blobClient = ((MainActivity) context).getStorageAccount().createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(MainActivity.KEY_CONTAINER);
            CloudBlockBlob blob = container.getBlockBlobReference(outputFile.getName());
            blob.upload(new java.io.FileInputStream(outputFile), outputFile.length());

            //saveEncryptedKeyToSharedPreferences(blobClient);

            //
        } catch (IOException | StorageException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (printStream != null) {
                printStream.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Toast.makeText(context, "Key has been uploaded", Toast.LENGTH_LONG).show();
        ((MainActivity) context).saveToSharedPreferences(CryptoUtils.ENCRYPTED_KEY, encryptedKey);
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
