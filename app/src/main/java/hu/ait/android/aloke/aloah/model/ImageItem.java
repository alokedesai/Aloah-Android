package hu.ait.android.aloke.aloah.model;

import com.microsoft.azure.storage.blob.ListBlobItem;

import java.io.File;

/**
 * Created by Aloke on 5/9/15.
 */
public class ImageItem {
    private ListBlobItem blob;
    private boolean isDownloaded;
    private File file;

    public ImageItem(ListBlobItem blob) {
        this.blob = blob;
    }

    public ListBlobItem getBlob() {
        return blob;
    }

    public void setBlob(ListBlobItem blob) {
        this.blob = blob;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
