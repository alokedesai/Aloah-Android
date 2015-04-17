package hu.ait.android.aloke.aloah.adapter;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.IOException;
import java.util.List;

import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.R;

/**
 * Created by Aloke on 4/15/15.
 */
public class BlobListAdapter extends BaseAdapter {
    private List<ListBlobItem> blobs;
    private Context context;


    public BlobListAdapter(List<ListBlobItem> blobs, Context context) {
        this.blobs = blobs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return blobs.size();
    }

    @Override
    public Object getItem(int position) {
        return blobs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_blob_item, null);
            ViewHolder holder = new ViewHolder();
            holder.tvBlobName = (TextView) v.findViewById(R.id.tvBlobName);
            holder.btnBlobDownload = (Button) v.findViewById(R.id.btnBlobDownload);

            v.setTag(holder);
        }

        final ListBlobItem blob = blobs.get(position);

        if (blob != null) {
            ViewHolder holder = (ViewHolder) v.getTag();
            holder.tvBlobName.setText(blob.getUri().toString());
            holder.btnBlobDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ((MainActivity) context).showKeyEntryDialog();
                    ((MainActivity) context).downloadFile(position);
                }
            });
        }

        return v;
    }

    static class ViewHolder {
        TextView tvBlobName;
        Button btnBlobDownload;
    }

}
