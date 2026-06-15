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

package com.comapi;

import android.os.Build;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link BaseClient#parsePushMessage(RemoteMessage)}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class ParsePushMessageTest {

    private static final String SENDER_ID = "123456789";

    private RemoteMessage buildMessage(Map<String, String> data) {
        RemoteMessage.Builder builder = new RemoteMessage.Builder(SENDER_ID);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            builder.addData(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    @Test
    public void testDdOriginatedTrue() throws JSONException {
        Map<String, String> data = new HashMap<>();
        data.put("dd_originated", "true");

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertTrue(result.isDdOriginated());
    }

    @Test
    public void testDdOriginatedTrueCaseInsensitive() throws JSONException {
        Map<String, String> data = new HashMap<>();
        data.put("dd_originated", "TRUE");

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertTrue(result.isDdOriginated());
    }

    @Test
    public void testDdOriginatedFalse() throws JSONException {
        Map<String, String> data = new HashMap<>();
        data.put("dd_originated", "false");

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertFalse(result.isDdOriginated());
    }

    @Test
    public void testDdOriginatedAbsent() throws JSONException {
        Map<String, String> data = new HashMap<>();

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertFalse(result.isDdOriginated());
    }

    @Test
    public void testDeepLinkParsedWithDdOriginated() throws JSONException {
        Map<String, String> data = new HashMap<>();
        data.put("dd_deepLink", "{\"url\":\"https://example.com\"}");
        data.put("dd_originated", "true");

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertEquals("https://example.com", result.getUrl());
        assertNull(result.getData());
        assertTrue(result.isDdOriginated());
    }

    @Test
    public void testDataPayloadParsedWithDdOriginated() throws JSONException {
        Map<String, String> data = new HashMap<>();
        data.put("dd_data", "{\"key\":\"value\"}");
        data.put("dd_originated", "true");

        PushDetails result = BaseClient.parsePushMessage(buildMessage(data));

        assertNotNull(result);
        assertNull(result.getUrl());
        assertNotNull(result.getData());
        assertEquals("value", result.getData().getString("key"));
        assertTrue(result.isDdOriginated());
    }
}
