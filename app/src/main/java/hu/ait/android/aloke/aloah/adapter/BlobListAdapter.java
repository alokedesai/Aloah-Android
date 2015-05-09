package hu.ait.android.aloke.aloah.adapter;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hu.ait.android.aloke.aloah.MainActivity;
import hu.ait.android.aloke.aloah.OnOverflowSelectedListener;
import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.model.ImageItem;

/**
 * Created by Aloke on 4/15/15.
 */
public class BlobListAdapter extends BaseAdapter {
    private List<ImageItem> blobs;
    private Context context;


    public BlobListAdapter(List<ImageItem> blobs, Context context) {
        this.blobs = blobs;
        this.context = context;
    }


    public void setIsDownloaded(int index) {
        blobs.get(index).setIsDownloaded(true);
        notifyDataSetChanged();
    }

    public void setFile(int index, File file){
        blobs.get(index).setFile(file);
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
            holder.tvLastModified =  (TextView) v.findViewById(R.id.tvLastModified);
            holder.ivOverflow = (ImageView) v.findViewById(R.id.ivOverflow);

            v.setTag(holder);
        }

        final ImageItem imageItem = blobs.get(position);

        if (imageItem != null) {
            final ViewHolder holder = (ViewHolder) v.getTag();

            final ListBlobItem blobItem = imageItem.getBlob();
            final CloudBlockBlob cloudBlockBlob = (CloudBlockBlob) blobItem;

            holder.tvLastModified.setText(getFormattedDate(cloudBlockBlob));

            try {
                holder.tvBlobName.setText(cloudBlockBlob.getName());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                holder.tvBlobName.setText(blobItem.getUri().toString());
            }

            holder.ivOverflow.setOnClickListener(new OnOverflowSelectedListener(context, imageItem, position));

        }

        return v;
    }

    private String getFormattedDate(CloudBlockBlob blobItem) {
        Date date = blobItem.getProperties().getLastModified();
        DateFormat df = new SimpleDateFormat("MM/dd 'at' hh:mm:ss", Locale.US);
        String formattedDate = df.format(date);
        return "Last modified " + formattedDate;
    }

    static class ViewHolder {
        TextView tvBlobName;
        TextView tvLastModified;
        ImageView ivOverflow;
    }

}
