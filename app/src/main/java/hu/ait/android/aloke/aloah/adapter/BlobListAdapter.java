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
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hu.ait.android.aloke.aloah.MainActivity;
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
            holder.btnBlobDownload = (Button) v.findViewById(R.id.btnBlobDownload);
            holder.btnViewImage = (Button) v.findViewById(R.id.btnViewImage);
            holder.tvLastModified =  (TextView) v.findViewById(R.id.tvLastModified);

            v.setTag(holder);
        }

        final ImageItem imageItem = blobs.get(position);

        if (imageItem != null) {
            final ViewHolder holder = (ViewHolder) v.getTag();

            final ListBlobItem blobItem = imageItem.getBlob();

            holder.tvLastModified.setText(getFormattedDate((CloudBlockBlob) blobItem));
            holder.tvBlobName.setText(blobItem.getUri().toString());
            holder.btnBlobDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloudBlockBlob b = ((CloudBlockBlob) blobItem);
                    ((MainActivity) context).downloadFile(b, position);

//                    if (!imageItem.isDownloaded()) {
//                        imageItem.setIsDownloaded(true);
//                        holder.btnViewImage.setVisibility(View.VISIBLE);
//                    }
                }
            });

            if (imageItem.isDownloaded()) {
                holder.btnViewImage.setVisibility(View.VISIBLE);
            }

            holder.btnViewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = imageItem.getFile();
                    Uri path = Uri.fromFile(file);
                    Intent imageOpenIntent = new Intent(Intent.ACTION_VIEW);
                    imageOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    imageOpenIntent.setDataAndType(path, "image/*");
                    context.startActivity(imageOpenIntent);
                }
            });
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
        Button btnBlobDownload;
        Button btnViewImage;
    }

}
