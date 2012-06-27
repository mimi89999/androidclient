/*
 * Kontalk Android client
 * Copyright (C) 2011 Kontalk Devteam <devteam@kontalk.org>

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

package org.kontalk.message;

import java.security.GeneralSecurityException;
import java.util.List;

import org.kontalk.R;
import org.kontalk.crypto.Coder;

import android.content.Context;
import android.database.Cursor;
import android.os.Message;

import com.google.protobuf.ByteString;


/**
 * A plain text message.
 * Mime type: text/plain
 * @author Daniele Ricci
 * @version 1.0
 */
public class PlainTextMessage extends AbstractMessage<ByteString> {
    //private static final String TAG = PlainTextMessage.class.getSimpleName();

    private static final Object sPoolSync = new Object();
    private static PlainTextMessage sPool;
    private static int sPoolSize = 0;

    /** Global pool max size. */
    private static final int MAX_POOL_SIZE = 10;

    /** Used for pooling. */
    protected PlainTextMessage next;

    public static final String MIME_TYPE = "text/plain";

    protected PlainTextMessage(Context context) {
        super(context, null, null, null, null, null, false);
    }

    public PlainTextMessage(Context context, String id, String timestamp, String sender, byte[] content, boolean encrypted) {
        this(context, id, timestamp, sender, content, encrypted, null);
    }

    public PlainTextMessage(Context context, String id, String timestamp, String sender, byte[] content, boolean encrypted, List<String> group) {
        super(context, id, timestamp, sender, MIME_TYPE, ByteString.copyFrom(content), encrypted, group);
    }

    @Override
    protected void populateFromCursor(Cursor c) {
        super.populateFromCursor(c);
        content = ByteString.copyFrom(c.getBlob(COLUMN_CONTENT));
    }

    @Override
    public String getTextContent() {
        if (encrypted)
            return mContext.getResources().getString(R.string.text_encrypted);
        return content.toStringUtf8();
    }

    @Override
    public byte[] getBinaryContent() {
        return content.toByteArray();
    }

    @Override
    public void decrypt(Coder coder) throws GeneralSecurityException {
        if (isEncrypted()) {
            byte[] buf = coder.decrypt(content.toByteArray());
            content = ByteString.copyFrom(buf);
            encrypted = false;
        }
    }

    public static boolean supportsMimeType(String mime) {
        return MIME_TYPE.equalsIgnoreCase(mime);
    }

    public void recycle() {
        clear();

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases. Inspired by {@link Message}.
     */
    public static PlainTextMessage obtain(Context context) {
        synchronized (sPoolSync) {
            if (sPool != null) {
                PlainTextMessage m = sPool;
                sPool = m.next;
                m.next = null;
                sPoolSize--;
                m.mContext = context;
                return m;
            }
        }
        return new PlainTextMessage(context);
    }

}
