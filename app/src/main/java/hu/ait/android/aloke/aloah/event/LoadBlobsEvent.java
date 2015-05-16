package hu.ait.android.aloke.aloah.event;

import java.util.ArrayList;

import hu.ait.android.aloke.aloah.model.DataItem;

/**
 * Created by Aloke on 5/15/15.
 */
public class LoadBlobsEvent {
    public ArrayList<DataItem> images;

    public LoadBlobsEvent(ArrayList<DataItem> images) {
        this.images = images;
    }
}
