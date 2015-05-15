package hu.ait.android.aloke.aloah;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import hu.ait.android.aloke.aloah.adapter.UnapprovedUsersListAdapter;


public class AdminActivity extends ActionBarActivity {
    List<ParseObject> usersToApprove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        final ListView listView = (ListView) findViewById(R.id.listView);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.whereEqualTo("approved", false);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    usersToApprove = scoreList;
                } else {
                    usersToApprove = new ArrayList<ParseObject>();
                }
                // set the list adapter
                listView.setAdapter(new UnapprovedUsersListAdapter(AdminActivity.this, usersToApprove));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //TODO: think of a cleaner way of doing this
    public CloudStorageAccount getStorageAccount() {
        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(MainActivity.STORAGE_CONNECTION_STRING);
        } catch(URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return storageAccount;
    }
}
