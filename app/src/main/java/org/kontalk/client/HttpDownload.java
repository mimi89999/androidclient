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

package org.kontalk.client;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;


/**
 * HttpDownload
 * @author Andrea Cappelli
 * @author Daniele Ricci
 */
public class HttpDownload extends Thread {
    private static final String TAG = HttpDownload.TAG;

    private String mUrl;
    private File mFile;
    private Runnable mSuccess;
    private Runnable mError;

    public HttpDownload (String url, File destination, Runnable success, Runnable error) {
        this.mUrl=url;
        this.mFile = destination;
        this.mSuccess=success;
        this.mError=error;
    }

    public void run() {
        HttpClient client = new DefaultHttpClient();
        HttpRequestBase req=new HttpGet(mUrl);
        try {
            HttpResponse resp=client.execute(req);
            if (resp.getStatusLine().getStatusCode()==200) {
                FileOutputStream out = new FileOutputStream(mFile);
                resp.getEntity().writeTo(out);
                out.close();
                resp.getEntity().consumeContent();

                mSuccess.run();
            }

            else {
                mError.run();
            }
        }
        catch (Exception e)
        {
            Log.e("Kontalk","Error Downloading File",e);
            mError.run();
        }

    }

}
