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

import android.app.Activity;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.kontalk.R;
import org.kontalk.data.Contact;
import org.kontalk.sync.SyncAdapter;
import org.kontalk.util.RunnableBroadcastReceiver;


/** Contacts list selection fragment. */
public class ContactsListFragment extends Fragment
        implements ContactsListAdapter.OnContentChangedListener, OnItemClickListener {

    private Cursor mCursor;
    private RecyclerView mRecyclerView;
    private ContactsListAdapter mListAdapter;

    private LocalBroadcastManager mBroadcastManager;

    private RunnableBroadcastReceiver mSyncMonitor;
    private Handler mHandler;

    private final RunnableBroadcastReceiver.ActionRunnable mPostSyncAction =
            new RunnableBroadcastReceiver.ActionRunnable() {
        public void run(String action) {
            if (SyncAdapter.ACTION_SYNC_START.equals(action)) {
                ((ContactsSyncActivity) getActivity()).setSyncing(true);
            }
            else if (SyncAdapter.ACTION_SYNC_FINISH.equals(action)) {
                startQuery();
                ((ContactsSyncActivity) getActivity()).setSyncing(false);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_list, container, false);

        TextView text = (TextView) view.findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.text_contacts_empty)));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity parent = getActivity();

        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(parent);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mListAdapter = new ContactsListAdapter(parent, null, mRecyclerView);
        mListAdapter.setOnContentChangedListener(this);
        mListAdapter.addOnItemClickListener(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mListAdapter);

        mHandler = new Handler();
        mBroadcastManager = LocalBroadcastManager.getInstance(parent);

        // retain current sync state to hide the refresh button and start indeterminate progress
        registerSyncReceiver();
        if (SyncAdapter.isActive(parent))
            ((ContactsSyncActivity) parent).setSyncing(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        mListAdapter.changeCursor(null);
        try {
            // make sure the cursor is really closed
            mCursor.close();
        }
        catch (Exception e) {
            // ignored
        }

        // cancel sync monitor
        if (mSyncMonitor != null) {
            mBroadcastManager.unregisterReceiver(mSyncMonitor);
            mSyncMonitor = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startQuery();
    }

    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ContactPickerListener parent = (ContactPickerListener) getActivity();

        if (parent != null)
            parent.onContactSelected(this, ((ContactsListItem) v).getContact());
    }*/

    private void registerSyncReceiver() {
        // register sync monitor
        if (mSyncMonitor == null) {
            mSyncMonitor = new RunnableBroadcastReceiver(mPostSyncAction, mHandler);
            IntentFilter filter = new IntentFilter
                            (SyncAdapter.ACTION_SYNC_FINISH);
            filter.addAction(SyncAdapter.ACTION_SYNC_START);
            mBroadcastManager.registerReceiver(mSyncMonitor, filter);
        }
    }

    public void startQuery() {
        mCursor = Contact.queryContacts(getActivity());
        mListAdapter.changeCursor(mCursor);
    }

    @Override
    public void onContentChanged(ContactsListAdapter adapter) {
        startQuery();
    }

    @Override
    public void onItemClick(View view, int position) {
        ContactPickerListener parent = (ContactPickerListener) getActivity();

        if (parent != null)
            parent.onContactSelected(this, ((ContactsListItem) view).getContact());
    }

    @Override
    public void onLongItemClick(View view, int position) {

    }
}
