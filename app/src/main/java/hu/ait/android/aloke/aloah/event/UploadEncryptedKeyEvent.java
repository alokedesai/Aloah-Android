package hu.ait.android.aloke.aloah.event;

/**
 * Created by Aloke on 5/15/15.
 */
public class UploadEncryptedKeyEvent {
    public final boolean success;

    public UploadEncryptedKeyEvent(boolean success) {
        this.success = success;
    }
}
