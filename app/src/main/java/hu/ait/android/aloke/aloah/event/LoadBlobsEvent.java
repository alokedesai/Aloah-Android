package hu.ait.android.aloke.aloah.event;

import java.util.ArrayList;
import java.util.List;

import hu.ait.android.aloke.aloah.model.ImageItem;

/**
 * Created by Aloke on 5/15/15.
 */
public class LoadBlobsEvent {
    public ArrayList<ImageItem> images;

    public LoadBlobsEvent(ArrayList<ImageItem> images) {
        this.images = images;
    }
}
