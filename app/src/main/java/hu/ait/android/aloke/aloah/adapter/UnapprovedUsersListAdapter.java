package hu.ait.android.aloke.aloah.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import hu.ait.android.aloke.aloah.R;
import hu.ait.android.aloke.aloah.task.UploadEncryptedKey;

/**
 * Created by Aloke on 5/14/15.
 */
public class UnapprovedUsersListAdapter extends BaseAdapter {
    private Context context;
    private List<ParseObject> users;

    public UnapprovedUsersListAdapter(Context context, List<ParseObject> users) {
        this.context = context;
        this.users = users;
    }


    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.row_unapproved_user_item, null);
            ViewHolder holder = new ViewHolder();
            holder.tvName = (TextView) v.findViewById(R.id.tvParseName);
            holder.btnApprove = (Button) v.findViewById(R.id.btnApprove);

            v.setTag(holder);
        }

        final ParseObject user = users.get(position);

        if (user != null) {
            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.tvName.setText(user.getString("username"));
            holder.btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask<String, Void, Boolean> asyncTask =
                            new UploadEncryptedKey(context, user, position);
                    asyncTask.execute(user.getString("publicKey"));
                }
            });
        }

        return v;
    }

    public void removeItem(int index) {
        users.remove(index);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        Button btnApprove;
        TextView tvName;
    }
}
