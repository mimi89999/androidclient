/*
 * Kontalk Android client
 * Copyright (C) 2015 Kontalk Devteam <devteam@kontalk.org>

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

import com.github.machinarius.preferencefragment.PreferenceFragment;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;

import org.kontalk.R;


/**
 * Base class for root preference fragments.
 * @author Daniele Ricci
 */
public class RootPreferenceFragment extends PreferenceFragment {

    private Callback mCallback;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupPreferences();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        }
        else {
            throw new IllegalStateException("Owner must implement Callback interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    void invokeCallback(int key) {
        if (mCallback != null)
            mCallback.onNestedPreferenceSelected(key);
    }

    protected void setupPreferences() {
        // privacy section
        final Preference privacy = findPreference("pref_privacy_settings");
        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                invokeCallback(R.xml.privacy_preferences);
                return true;
            }
        });
    }

    public interface Callback {
        public void onNestedPreferenceSelected(int key);
    }

}
