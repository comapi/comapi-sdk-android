/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Comapi (trading name of Dynmark International Limited)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.comapi.internal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comapi.internal.push.PushDataKeys;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (PushDataKeys.PUSH_CLICK_ACTION.equals(intent.getAction())) {
            String link = intent.getStringExtra(PushDataKeys.KEY_PUSH_DEEP_LINK);
            if (link != null) {
                Intent i = new Intent(PushDataKeys.PUSH_CLICK_ACTION);
                i.putExtra(PushDataKeys.KEY_PUSH_ACTION_ID, intent.getStringExtra(PushDataKeys.KEY_PUSH_ACTION_ID));
                i.putExtra(PushDataKeys.KEY_PUSH_CORRELATION_ID, intent.getStringExtra(PushDataKeys.KEY_PUSH_CORRELATION_ID));
                i.putExtra(PushDataKeys.KEY_PUSH_DEEP_LINK, link);
                LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}