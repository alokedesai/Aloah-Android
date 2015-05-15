package hu.ait.android.aloke.aloah.event;

/**
 * Created by Aloke on 5/15/15.
 */
public class DownloadEncryptedKeyEvent {
    public final String encryptedKey;

    public DownloadEncryptedKeyEvent(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
}
