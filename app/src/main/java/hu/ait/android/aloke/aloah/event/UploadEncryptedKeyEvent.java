package hu.ait.android.aloke.aloah.event;

/**
 * Created by Aloke on 5/15/15.
 */
public class UploadEncryptedKeyEvent {
    public final boolean success;
    public final int listIndex;

    public UploadEncryptedKeyEvent(int listIndex, boolean success) {
        this.listIndex = listIndex;
        this.success = success;
    }
}
