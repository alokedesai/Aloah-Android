package hu.ait.android.aloke.aloah;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.lang.reflect.Field;

import hu.ait.android.aloke.aloah.model.DataItem;

/**
 * Created by Aloke on 5/9/15.
 */
public class OnOverflowSelectedListener implements View.OnClickListener{
    private DataItem dataItem;
    private Context context;
    private int position;

    public OnOverflowSelectedListener(Context context, DataItem dataItem, int position) {
        this.context = context;
        this.dataItem = dataItem;
        this.position = position;
    }

    @Override
    public void onClick(View v) {
        // This is an android.support.v7.widget.PopupMenu;
        PopupMenu popupMenu = new PopupMenu(context, v) {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_overflow_download:
                        ((MainActivity) context).downloadFile((CloudBlockBlob) dataItem.getBlob(), position);
                        return true;
                    case R.id.item_overflow_preview:
                        ((MainActivity) context).openImageFromImageItem(dataItem);
                        return true;
                    case R.id.item_overflow_delete:
                        ((MainActivity) context).deleteFile((CloudBlockBlob) dataItem.getBlob());
                    default:
                        return super.onMenuItemSelected(menu, item);
                }
            }
        };

        popupMenu.inflate(R.menu.menu_overflow);

        if (!dataItem.isDownloaded()) {
            popupMenu.getMenu().removeItem(R.id.item_overflow_preview);
        }

        // Force icons to show using a crazy Java reflection hack
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[] { boolean.class };
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.

            popupMenu.show();
            return;
        }

        popupMenu.show();
    }
}
