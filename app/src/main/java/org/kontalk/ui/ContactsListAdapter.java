/*
 * Kontalk Android client
 * Copyright (C) 2014 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CursorAdapter;
import android.widget.ListView;

import org.kontalk.R;
import org.kontalk.data.Contact;
import org.kontalk.data.Conversation;


public class ContactsListAdapter extends CursorRecyclerViewAdapter<ContactsListAdapter.ViewHolder> {
    private static final String TAG = ContactsListActivity.TAG;

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private OnItemClickListener mItemClickListener;
    private View mView;

    public ContactsListAdapter(Context context, Cursor cursor, RecyclerView list) {
        super(context, cursor);
        mFactory = LayoutInflater.from(context);

        /*list.setRecyclerListener(new RecyclerListener() {
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    ((ContactsListItem) view).unbind();
                }
            }
        });*/
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Context context, Cursor cursor) {
        if (!(viewHolder.mHeaderview instanceof ContactsListItem)) {
            Log.e(TAG, "Unexpected bound view: " + viewHolder);
            return;
        }

        Contact contact = Contact.fromUsersCursor(context, cursor);
        viewHolder.mHeaderview.bind(context, contact);
        mView = viewHolder.mHeaderview;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = mFactory.inflate(R.layout.contacts_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ContactsListItem mHeaderview;
        public ViewHolder(View view) {
            super(view);
            mHeaderview = (ContactsListItem) view;
            mHeaderview.setOnClickListener(this);
            mHeaderview.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onLongItemClick(v, getPosition());
                return true;
            }
            return false;
        }
    }

    public void addOnItemClickListener(final org.kontalk.ui.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void addOnLongItemClickListener(final org.kontalk.ui.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnContentChangedListener {
        void onContentChanged(ContactsListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    @Override
    protected void onContentChanged() {
        Cursor c = getCursor();
        if (c != null && !c.isClosed() && mOnContentChangedListener != null) {
            mOnContentChangedListener.onContentChanged(this);
        }
    }
}
