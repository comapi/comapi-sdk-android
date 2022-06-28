package com.comapi.internal.push;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.io.Serializable;

import com.comapi.BuildConfig;
import com.comapi.internal.push.PushDataKeys;

import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.receivers.PushBroadcastReceiver;
import com.google.firebase.messaging.RemoteMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author Marcin Swierczek
 * @since 1.3.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
@LooperMode(LooperMode.Mode.PAUSED)
public class CustomPushWithAction {

    private PushBroadcastReceiver receiver;
    private Boolean messageReceived;
    private Boolean notificationHandled;
    private Boolean clickHandled;
    private final String trackingUrl = "url";
    private final String body = "body";
    private final String title = "title";
    private final String link = "http://google.com";
    private final String action = "notificationClick";
    private final String actionId = "actionID";
    private final String correlationId = "correlationId";

    public void setUp() {

        notificationHandled = false;
        messageReceived = false;

        receiver = new PushBroadcastReceiver(new Handler(Looper.getMainLooper()),
                () -> "token",
                token -> {
                },
                new MyMessageListener(),
                new LocalNotificationsManager(RuntimeEnvironment.application, new Logger(new LogManager(), "")) {
                    @Override
                    public void handleNotification(PushBuilder builder) {
                        assertEquals(title, builder.getTitle());
                        assertEquals(CustomPushWithAction.this.trackingUrl, builder.getTrackingUrl());
                        assertEquals(body, builder.getBody());
                        notificationHandled = true;
                    }
                    @Override
                    public void handleNotificationClick(String correlationId, Serializable data, String link) {
                        assertEquals(CustomPushWithAction.this.trackingUrl, trackingUrl);
                        assertEquals(CustomPushWithAction.this.link, link);
                        clickHandled = true;
                    }
                },
                new Logger(new LogManager(), ""));
    }

    @Test
    public void testActionMessage() {
        setUp();
        Intent i = new Intent(PushService.ACTION_PUSH_MESSAGE);
        RemoteMessage rm = new RemoteMessage(new Bundle());
        i.putExtra(PushService.KEY_MESSAGE, rm);
        receiver.onReceive(RuntimeEnvironment.application, i);
        shadowOf(Looper.getMainLooper()).idle();
        assertTrue(messageReceived);
    }

    @Test
    public void testCreateIntent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        LocalNotificationsManager lnm = new LocalNotificationsManager(RuntimeEnvironment.application, new Logger(new LogManager(), ""));

        Method method = lnm.getClass().getDeclaredMethod("createDeepLinkIntent", String.class, Serializable.class, String.class);
        method.setAccessible(true);
        Intent i = (Intent) method.invoke(lnm, "correlationId", new HashMap(), "link");

        assertEquals(Intent.ACTION_VIEW, i.getAction());
        assertTrue(i.getCategories().contains(Intent.CATEGORY_DEFAULT));
        assertEquals(Intent.ACTION_VIEW, i.getAction());

        Method methodAvailable = lnm.getClass().getDeclaredMethod("isActivityAvailable", Context.class, Intent.class);
        methodAvailable.setAccessible(true);
        Boolean isAvailable = (Boolean) methodAvailable.invoke(lnm, RuntimeEnvironment.application, i);
        assertFalse(isAvailable);
    }
    
    @Test
    public void testActionClick() {
        setUp();
        Intent i = new Intent(PushDataKeys.PUSH_CLICK_ACTION);
        i.putExtra(PushDataKeys.KEY_PUSH_TRACKING_URL, trackingUrl);
        i.putExtra(PushDataKeys.KEY_PUSH_DEEP_LINK, link);
        receiver.onReceive(RuntimeEnvironment.application, i);
        assertTrue(clickHandled);
    }

    @Test
    public void testMessageParse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        setUp();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put(PushDataKeys.KEY_PUSH_DEEP_LINK, String.format("{url:\"%s\",%s:\"%s\"}", link, PushDataKeys.KEY_PUSH_TRACKING_URL, CustomPushWithAction.this.trackingUrl));

        Method method = receiver.getClass().getDeclaredMethod("handleData", HashMap.class);
        method.setAccessible(true);
        method.invoke(receiver, data);
        assertTrue(notificationHandled);
    }

    class MyMessageListener implements PushMessageListener {

        @Override
        public void onMessageReceived(RemoteMessage message) {
            messageReceived = true;
        }
    }
}
