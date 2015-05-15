package hu.ait.android.aloke.aloah.event;

import java.io.File;

/**
 * Created by Aloke on 5/15/15.
 */
public class DownloadFileEvent {
    public final boolean success;
    public final File outputFile;
    public final int index;

    public DownloadFileEvent(boolean success) {
        this.success = success;
        outputFile = null;
        index = -1;
    }

    public DownloadFileEvent(int index, boolean success, File outputFile) {
        this.index = index;
        this.success = success;
        this.outputFile = outputFile;
    }
}
