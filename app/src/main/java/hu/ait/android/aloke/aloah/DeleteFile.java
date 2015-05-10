package hu.ait.android.aloke.aloah;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

/**
 * Created by Aloke on 5/10/15.
 */
public class DeleteFile extends AsyncTask<CloudBlockBlob, Void, Boolean> {
    public static final String FILTER_RESULT = "FILTER_RESULT";
    private Context context;
    private ProgressDialog progressDialog;

    public DeleteFile(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Deleting File");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(CloudBlockBlob... params) {
        CloudBlockBlob blob = params[0];
        boolean success = false;
        try {
            success = blob.deleteIfExists();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();
        if (success) {
            Toast.makeText(context, "The file was successfully deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "The file was unable to be deleted.", Toast.LENGTH_SHORT).show();
        }

    }
}
