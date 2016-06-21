package com.smartisanos.sidebar.view;

import java.util.List;

import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;

public class ContactListAdapter extends BaseAdapter {
    private static final String TAG = ContactListAdapter.class.getName();

    private Context mContext;
    private ContactManager mManager;
    private List<ContactItem> mContacts;

    public ContactListAdapter(Context context) {
        mContext = context;
        mManager = ContactManager.getInstance(mContext);
        mContacts = mManager.getContactList();
        mManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mContacts = mManager.getContactList();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return mContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View ret;
        final ViewHolder holder;
        if (convertView == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
            holder = new ViewHolder();
            holder.contactIcon = (ImageView) ret.findViewById(R.id.contact_icon);
            holder.typeIcon = (ImageView) ret.findViewById(R.id.type_icon);
            holder.displayName = (TextView) ret.findViewById(R.id.display_name);
            ret.setTag(holder);
        }else{
            ret = convertView;
            holder = (ViewHolder) ret.getTag();
        }
        final Bitmap avatar = mContacts.get(position).getAvatar();
        holder.contactIcon.setImageBitmap(BitmapUtils.convertToBlackWhite(avatar));
        holder.typeIcon.setImageResource(mContacts.get(position).getTypeIcon());
        holder.displayName.setText(mContacts.get(position).getDisplayName());

        ret.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                ContactItem ri = mContacts.get(position);
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    boolean accept = ri.accptDragEvent(event);
                    if(accept){
                        holder.contactIcon.setImageBitmap(avatar);
                    }
                    Log.d(TAG, "ACTION_DRAG_ENTERED");
                    return accept;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "ACTION_DRAG_ENTERED");
                    holder.displayName.setVisibility(View.VISIBLE);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "ACTION_DRAG_EXITED");
                    holder.displayName.setVisibility(View.GONE);
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "ACTION_DRAG_LOCATION");
                    return true;
                case DragEvent.ACTION_DROP:
                    boolean ret =  ri.handleDragEvent(event);
                    if(ret){
                        Utils.dismissAllDialog(mContext);
                    }
                    return ret;
                case DragEvent.ACTION_DRAG_ENDED:
                    holder.contactIcon.setImageBitmap(BitmapUtils.convertToBlackWhite(avatar));
                    return true;
                }
                return false;
            }
        });
        return ret;
    }

    class ViewHolder {
        public ImageView contactIcon;
        public ImageView typeIcon;
        public TextView displayName;
    }
}
