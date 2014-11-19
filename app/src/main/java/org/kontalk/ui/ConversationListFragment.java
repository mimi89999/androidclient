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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.kontalk.R;
import org.kontalk.data.Contact;
import org.kontalk.data.Conversation;
import org.kontalk.provider.MessagesProvider;
import org.kontalk.util.Preferences;


public class ConversationListFragment extends Fragment implements ConversationListAdapter.OnItemClickListener, android.support.v7.view.ActionMode.Callback {
    private static final String TAG = ConversationList.TAG;

    private static final int THREAD_LIST_QUERY_TOKEN = 8720;

    private ThreadListQueryHandler mQueryHandler;
    private RecyclerView mRecyclerView;
    private ConversationListAdapter mListAdapter;
    private boolean mDualPane;

    /** Search menu item. */
    private MenuItem mSearchMenu;
    private MenuItem mDeleteAllMenu;
    /** Offline mode menu item. */
    private MenuItem mOfflineMenu;

    private ActionMode mActionMode;

    private SearchView mSearchView;
    private SearchManager mSearchManager;

    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            startQuery();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversation_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mQueryHandler = new ThreadListQueryHandler(getActivity().getContentResolver());
        mListAdapter = new ConversationListAdapter(getActivity(), null, mRecyclerView);
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        mListAdapter.addOnItemClickListener(this);
        mListAdapter.addOnLongItemClickListener(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = getActivity().findViewById(R.id.fragment_compose_message);
        mDualPane = detailsFrame != null
                && detailsFrame.getVisibility() == View.VISIBLE;


        // add Compose message entry only if are in dual pane mode
        if (!mDualPane) {
            /*LayoutInflater inflater = getLayoutInflater(savedInstanceState);
            ConversationListItem headerView = (ConversationListItem)
                    inflater.inflate(R.layout.conversation_list_item, list, false);
            headerView.bind(getString(R.string.new_message),
                    getString(R.string.create_new_message));
            list.addHeaderView(headerView, null, true);*/
        }
        else {
            // TODO restore state
            /*list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            list.setItemsCanFocus(true);*/
        }

        // text for empty conversation list
        TextView text = (TextView) getActivity().findViewById(android.R.id.empty);
        text.setText(Html.fromHtml(getString(R.string.text_conversations_empty)));

        mRecyclerView.setAdapter(mListAdapter);
        registerForContextMenu(mRecyclerView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO save state
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_list_menu, menu);

        // compose message
        /*
        MenuItem item = menu.findItem(R.id.menu_compose);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        */

        // search
        mSearchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchMenu = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenu);
        mSearchView.setSearchableInfo(mSearchManager.getSearchableInfo(getActivity().getComponentName()));

        //MenuItemCompat.setShowAsAction(mSearchMenu, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        mDeleteAllMenu = menu.findItem(R.id.menu_delete_all);

        // offline mode
        mOfflineMenu = menu.findItem(R.id.menu_offline);

        // trigger manually
        onDatabaseChanged();
        updateOffline();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_compose:
                chooseContact();
                return true;

            case R.id.menu_status:
                StatusActivity.start(getActivity());
                return true;

            case R.id.menu_offline:
                final Context ctx = getActivity();
                final boolean currentMode = Preferences.getOfflineMode(ctx);
                if (!currentMode && !Preferences.getOfflineModeUsed(ctx)) {
                    // show offline mode warning
                    new AlertDialog.Builder(ctx)
                        .setTitle(R.string.title_offline_mode_warning)
                        .setMessage(R.string.message_offline_mode_warning)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Preferences.setOfflineModeUsed(ctx);
                                switchOfflineMode();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                }
                else {
                    switchOfflineMode();
                }
                return true;

            /*case R.id.menu_search:
                getActivity().onSearchRequested();
                return true;*/

            case R.id.menu_delete_all:
                deleteAll();
                return true;

            case R.id.menu_donate:
                launchDonate();
                return true;

            case R.id.menu_settings: {
                PreferencesActivity.start(getActivity());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchDonate() {
        Intent i = new Intent(getActivity(), AboutActivity.class);
        i.setAction(AboutActivity.ACTION_DONATION);
        startActivity(i);
    }

    private void deleteThread(long threadId) {
        MessagesProvider.deleteThread(getActivity(), threadId);
        MessagingNotification.updateMessagesNotification(getActivity().getApplicationContext(), false);
    }

    public void chooseContact() {
        ConversationList parent = getParentActivity();
        if (parent != null)
            parent.showContactPicker();
    }

    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_delete_all);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(R.string.confirm_will_delete_all);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MessagesProvider.deleteDatabase(getActivity());
                MessagingNotification.updateMessagesNotification(getActivity().getApplicationContext(), false);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    public ConversationList getParentActivity() {
        return (ConversationList) getActivity();
    }

    public void startQuery() {
        Cursor c = null;
        try {
            c = Conversation.startQuery(getActivity());
        }
        catch (SQLiteException e) {
            Log.e(TAG, "query error", e);
        }
        mQueryHandler.onQueryComplete(THREAD_LIST_QUERY_TOKEN, null, c);
    }

    @Override
    public void onStart() {
        super.onStart();
        startQuery();
    }

    @Override
    public void onResume() {
        super.onResume();

        // update offline mode
        updateOffline();
    }

    @Override
    public void onStop() {
        super.onStop();
        mListAdapter.changeCursor(null);
    }

    /** Used only in fragment contexts. */
    public void endConversation(ComposeMessageFragment composer) {
        getFragmentManager().beginTransaction().remove(composer).commit();
    }

    public final boolean isFinishing() {
        return (getActivity() == null ||
                (getActivity() != null && getActivity().isFinishing())) ||
                isRemoving();
    }

    /* Updates various UI elements after a database change. */
    private void onDatabaseChanged() {
        boolean visible = (mListAdapter != null && !mListAdapter.isEmpty());
        if (mSearchMenu != null) {
            mSearchMenu.setEnabled(visible).setVisible(visible);
        }
        // if it's null it hasn't gone through onCreateOptionsMenu() yet
        if (mSearchMenu != null) {
            mSearchMenu.setEnabled(visible).setVisible(visible);
            mDeleteAllMenu.setEnabled(visible).setVisible(visible);
        }
    }

    /** Updates offline mode menu. */
    private void updateOffline() {
        if (mOfflineMenu != null) {
            boolean offlineMode = Preferences.getOfflineMode(getActivity());
            int icon = (offlineMode) ? R.drawable.ic_menu_start_conversation :
                android.R.drawable.ic_menu_close_clear_cancel;
            int title = (offlineMode) ? R.string.menu_online : R.string.menu_offline;
            mOfflineMenu.setIcon(icon);
            mOfflineMenu.setTitle(title);
        }
    }

    private void switchOfflineMode() {
        Context ctx = getActivity();
        boolean currentMode = Preferences.getOfflineMode(ctx);
        Preferences.switchOfflineMode(ctx);
        updateOffline();
        // notify the user about the change
        int text = (currentMode) ? R.string.going_online : R.string.going_offline;
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        if (mActionMode != null) {
            myToggleSelection(position);
            if (mListAdapter.getSelectedItemCount() > 1) {
                mActionMode.getMenu().removeItem(R.id.conversation_list_open);
                mActionMode.getMenu().removeItem(R.id.conversation_list_contact);
            }
            //TODO
            /*else if(mListAdapter.getSelectedItemCount() == 1) {
                mActionMode.getMenu().clear();
                mActionMode.getMenuInflater().inflate(R.menu.conversation_list_menu_cab, mActionMode.getMenu());
            }*/
            else if (mListAdapter.getSelectedItemCount() < 1) {
                mActionMode.finish();
            }
        }
        else {
            ConversationListItem cv = (ConversationListItem) view;
            Conversation conv = cv.getConversation();

            ConversationList parent = getParentActivity();
            if (parent != null)
                parent.openConversation(conv, position);
        }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        mActionMode = getParentActivity().startSupportActionMode(this);
        myToggleSelection(position);
    }

    private void myToggleSelection(int idx) {
        mListAdapter.toggleSelection(idx);
        String title = getString(R.string.selected_count, mListAdapter.getSelectedItemCount());
        mActionMode.setTitle(title);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public CursorRecyclerViewAdapter getAdapter() {
        return mListAdapter;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.conversation_list_menu_cab, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        ConversationListItem vitem = (ConversationListItem) mListAdapter.getView();
        Conversation conv = vitem.getConversation();

        switch (menuItem.getItemId()) {
            case R.id.conversation_list_open:
                ConversationList parent = getParentActivity();
                if (parent != null) {
                    parent.openConversation(conv, mListAdapter.getCursor().getPosition());
                }
                mActionMode.finish();
                return true;
            case R.id.conversation_list_contact:
                Contact contact = conv.getContact();
                if (contact != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, contact.getUri()));
                }
                mActionMode.finish();
                return true;
            case R.id.conversation_list_delete:
                LinkedHashMap<Integer, Long> selectedItemPositions = mListAdapter.getSelectedItems();
                for (Object key : selectedItemPositions.keySet()) {
                    long value = selectedItemPositions.get(key);
                    deleteThread(value);
                }
                mActionMode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
        mListAdapter.clearSelections();
    }

    /**
     * The conversation list query handler.
     */
    private final class ThreadListQueryHandler extends AsyncQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor == null || isFinishing()) {
                // close cursor - if any
                if (cursor != null) cursor.close();

                Log.w(TAG, "query aborted or error!");
                mListAdapter.changeCursor(null);
                return;
            }

            switch (token) {
                case THREAD_LIST_QUERY_TOKEN:
                    mListAdapter.changeCursor(cursor);
                    onDatabaseChanged();
                    break;

                default:
                    Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }
    }

    public boolean isDualPane() {
        return mDualPane;
    }

}
