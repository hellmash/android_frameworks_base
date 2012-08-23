/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.pm;

import com.android.internal.util.XmlUtils;
import com.android.server.PreferredComponent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;

class PreferredActivity extends IntentFilter implements PreferredComponent.Callbacks {
    private static final String TAG = "PreferredActivity";

    private static final boolean DEBUG_FILTERS = false;
    static final String ATTR_USER_ID = "userId";

    final PreferredComponent mPref;
    final int mUserId;

    PreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        this(filter, match, set, activity, 0);
    }

    PreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity,
            int userId) {
        super(filter);
        mUserId = userId;
        mPref = new PreferredComponent(this, match, set, activity);
    }

    PreferredActivity(XmlPullParser parser) throws XmlPullParserException, IOException {
        String userIdString = parser.getAttributeValue(null, ATTR_USER_ID);
        if (userIdString != null && userIdString.length() > 0) {
            mUserId = Integer.parseInt(userIdString);
        } else {
            // Old format with no userId specified - assume primary user
            mUserId = 0;
        }
        mPref = new PreferredComponent(this, parser);
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        serializer.attribute(null, ATTR_USER_ID, Integer.toString(mUserId));
        mPref.writeToXml(serializer);
        serializer.startTag(null, "filter");
            super.writeToXml(serializer);
        serializer.endTag(null, "filter");
    }

    public boolean onReadTag(String tagName, XmlPullParser parser) throws XmlPullParserException,
            IOException {
        if (tagName.equals("filter")) {
            if (DEBUG_FILTERS) {
                Log.i(TAG, "Starting to parse filter...");
            }
            readFromXml(parser);
            if (DEBUG_FILTERS) {
                Log.i(TAG, "Finished filter: depth=" + parser.getDepth() + " tag="
                        + parser.getName());
            }
        } else {
            PackageManagerService.reportSettingsProblem(Log.WARN,
                    "Unknown element under <preferred-activities>: " + parser.getName());
            XmlUtils.skipCurrentTag(parser);
        }
        return true;
    }
}