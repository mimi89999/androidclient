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

package org.kontalk.ui.view;

import java.util.regex.Pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.kontalk.R;
import org.kontalk.message.CompositeMessage;
import org.kontalk.message.VCardComponent;
import org.kontalk.util.Preferences;


/**
 * Message component for {@link org.kontalk.message.VCardComponent}.
 * @author Daniele Ricci
 */
public class VCardContentView extends TextView
    implements MessageContentView<VCardComponent> {

    private VCardComponent mComponent;

    public VCardContentView(Context context) {
        super(context);
    }

    public VCardContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VCardContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void bind(long id, VCardComponent component, Pattern highlight) {
        mComponent = component;

        Context context = getContext();
        String size = Preferences.getFontSize(context);
        int sizeId;
        if (size.equals("small"))
            sizeId = android.R.style.TextAppearance_Small;
        else if (size.equals("large"))
            sizeId = android.R.style.TextAppearance_Large;
        else
            sizeId = android.R.style.TextAppearance;
        setTextAppearance(context, sizeId);

        String text = CompositeMessage.getSampleTextContent(component.getMime());
        setText(text);
    }

    @Override
    public void unbind() {
        clear();
    }

    @Override
    public VCardComponent getComponent() {
        return mComponent;
    }

    @Override
    public int getPriority() {
        return 7;
    }

    private void clear() {
        mComponent = null;
    }

    public static VCardContentView create(LayoutInflater inflater, ViewGroup parent) {
        VCardContentView view = (VCardContentView) inflater.inflate(R.layout.message_content_vcard,
            parent, false);
        return view;
    }

}
