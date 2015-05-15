package hu.ait.android.aloke.aloah.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import de.greenrobot.event.EventBus;
import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.event.DeleteFileEvent;

/**
 * Created by Aloke on 5/10/15.
 */
public class DeleteFile extends AsyncTask<CloudBlockBlob, Void, Boolean> {
    private Context context;
    private ProgressDialog progressDialog;

    public DeleteFile(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage(context.getString(R.string.deleting_file_progress_dialog_text));
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
        EventBus.getDefault().post(new DeleteFileEvent(success));
    }
}
