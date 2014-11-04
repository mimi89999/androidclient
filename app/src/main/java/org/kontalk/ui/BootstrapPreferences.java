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

import org.kontalk.R;
import org.kontalk.service.msgcenter.PushServiceManager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;
import com.github.machinarius.preferencefragment.PreferenceFragment;


/**
 * PreferencesFragment fragment for some bootstrap preferences.
 * @author Daniele Ricci
 * @version 1.0
 */
public class BootstrapPreferences extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bootstrap_preferences);

        // disable push notifications if GCM is not available on the device
        if (!PushServiceManager.getInstance(getActivity()).isServiceAvailable()) {
            final Preference push = findPreference("pref_push_notifications");
            push.setEnabled(false);
        }

        PreferencesFragment.setupPreferences(this);
    }
}
