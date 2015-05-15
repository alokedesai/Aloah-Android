package hu.ait.android.aloke.aloah.task;

import android.os.AsyncTask;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.event.DownloadEncryptedKeyEvent;

/**
 * Created by Aloke on 5/15/15.
 */
public class DownloadEncryptedKey extends AsyncTask<String, Void, String> {
    private CloudStorageAccount storageAccount;
    private File cacheDir;

    public DownloadEncryptedKey(CloudStorageAccount storageAccount, File cacheDir) {
        this.storageAccount = storageAccount;
        this.cacheDir = cacheDir;
    }

    @Override
    protected String doInBackground(String... params) {
        String userId = params[0];
        String encryptedKey = null;
        try {
            storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer keyContainer = blobClient.getContainerReference(MainActivity.KEY_CONTAINER);
            CloudBlockBlob blobEncryptedKey = keyContainer.getBlockBlobReference(userId + ".txt");
            File tempKeyFile = File.createTempFile("tempkeyfile_download", ".tmp", cacheDir);

            blobEncryptedKey.downloadToFile(tempKeyFile.getAbsolutePath());

            BufferedReader brTest = new BufferedReader(new FileReader(tempKeyFile));
            encryptedKey =  brTest.readLine();

        } catch (InvalidKeyException | StorageException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return encryptedKey;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            EventBus.getDefault().post(new DownloadEncryptedKeyEvent(s));
        }
    }
}
