package hu.ait.android.aloke.aloah.event;

/**
 * Created by Aloke on 5/15/15.
 */
public class DeleteFileEvent {
    public final boolean success;

    public DeleteFileEvent(boolean success) {
        this.success = success;
    }
}
