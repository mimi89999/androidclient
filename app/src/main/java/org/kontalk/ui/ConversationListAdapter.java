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

import java.util.LinkedHashMap;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kontalk.R;
import org.kontalk.data.Conversation;



public class ConversationListAdapter extends CursorRecyclerViewAdapter<ConversationListAdapter.ViewHolder> {
    private static final String TAG = ConversationList.TAG;

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    OnItemClickListener mItemClickListener;
    private LinkedHashMap<Integer, Long> mSelectedItems;
    private View mView;
    Conversation mConv;
    private Long mThreadId;

    public ConversationListAdapter(Context context, Cursor cursor, RecyclerView list) {
        super(context, cursor);
        mFactory = LayoutInflater.from(context);

        mSelectedItems = new LinkedHashMap<Integer, Long>();

        /*list.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof MessageListItem) {
                    ((ConversationListItem) viewHolder).unbind();
                }
            }
        });*/
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Context context, Cursor cursor) {
        if (!(viewHolder.mHeaderview instanceof ConversationListItem)) {
            Log.e(TAG, "Unexpected bound view: " + viewHolder);
            return;
        }

        //ConversationListItem headerView = (ConversationListItem) viewHolder;
        mConv = Conversation.createFromCursor(context, cursor);
        viewHolder.mHeaderview.bind(context, mConv);
        viewHolder.mHeaderview.setSelected(mSelectedItems.containsKey(cursor.getPosition()));
        mView = viewHolder.mHeaderview;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = mFactory.inflate(R.layout.conversation_list_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ConversationListItem mHeaderview;
        public ViewHolder(View view) {
            super(view);
            mHeaderview = (ConversationListItem) view;
            mHeaderview.setOnClickListener(this);
            mHeaderview.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mThreadId = mHeaderview.getConversation().getThreadId();
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemClickListener != null) {
                mThreadId = mHeaderview.getConversation().getThreadId();
                mItemClickListener.onLongItemClick(v, getPosition());
                return true;
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onLongItemClick(View view, int position);
    }

    public void addOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void addOnLongItemClickListener(final  OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter adapter);
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

    public void toggleSelection(int pos) {
        if (mSelectedItems.containsKey(pos)) {
            mSelectedItems.remove(pos);
        }
        else {
            mSelectedItems.put(pos, mThreadId);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public LinkedHashMap<Integer, Long> getSelectedItems() {
        return mSelectedItems;
    }

    public View getView() {
        return mView;
    }

}
