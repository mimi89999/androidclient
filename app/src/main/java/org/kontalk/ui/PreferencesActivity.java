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
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.kontalk.R;
import org.kontalk.authenticator.Authenticator;

/**
 * PreferencesActivity.
 * @author Daniele Ricci
 * @author Andrea Cappelli
 */
public class PreferencesActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preference_activity);

        // no account - redirect to bootstrap preferences
        if (Authenticator.getDefaultAccount(this) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new BootstrapPreferences())
                    .commit();
        }
        else
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new PreferencesFragment())
                .commit();

        setupActivity();
    }

    private void setupActivity() {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Authenticator.getDefaultAccount(this) == null) {
                    finish();
                    return true;
                }
                else {
                    finish();
                    startActivity(new Intent(this, ConversationList.class));
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public static void start(Activity context) {
        Intent intent = new Intent(context, PreferencesActivity.class);
        context.startActivityIfNeeded(intent, -1);
    }
}
