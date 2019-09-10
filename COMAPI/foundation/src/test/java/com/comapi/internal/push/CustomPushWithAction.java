package com.comapi.internal.push;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.comapi.BuildConfig;
import com.comapi.internal.log.LogManager;
import com.comapi.internal.log.Logger;
import com.comapi.internal.receivers.PushBroadcastReceiver;
import com.google.firebase.messaging.RemoteMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Marcin Swierczek
 * @since 1.3.0
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M, constants = BuildConfig.class, packageName = "com.comapi")
public class CustomPushWithAction {

    private PushBroadcastReceiver receiver;
    private Boolean messageReceived;
    private Boolean notificationHandled;
    private Boolean clickHandled;
    private String messageId = "msgID";
    private String title = "title";
    private String link = "http://google.com";
    private String action = "notificationClick";
    private String actionId = "actionID";

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
                        assertEquals(messageId, builder.getMessageId());
                        assertEquals(title, builder.getTitle());
                        assertEquals(link, builder.getClickActionDetails().get("link"));
                        assertEquals(action, builder.getClickActionDetails().get("action"));
                        assertEquals(actionId, builder.getClickActionDetails().get("id"));
                        notificationHandled = true;
                    }
                    @Override
                    public void handleNotificationClick(String messageId, String id, String link) {
                        assertEquals(CustomPushWithAction.this.messageId, messageId);
                        assertEquals(CustomPushWithAction.this.link, link);
                        assertEquals(CustomPushWithAction.this.actionId, id);
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
        assertTrue(messageReceived);
    }

    @Test
    public void testCreateIntent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        LocalNotificationsManager lnm = new LocalNotificationsManager(RuntimeEnvironment.application, new Logger(new LogManager(), ""));

        Method method = lnm.getClass().getDeclaredMethod("createDeepLinkIntent", String.class);
        method.setAccessible(true);
        Intent i = (Intent) method.invoke(lnm, "link");

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
        i.putExtra(PushDataKeys.KEY_PUSH_MESSAGE_ID, messageId);
        i.putExtra(PushDataKeys.KEY_ACTION_ID, actionId);
        i.putExtra(PushDataKeys.KEY_DEEP_LINK, link);
        receiver.onReceive(RuntimeEnvironment.application, i);
        assertTrue(clickHandled);
    }

    @Test
    public void testMessageParse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        setUp();

        Map<String, String> data = new HashMap<>();
        data.put(PushDataKeys.KEY_PUSH_MAIN, String.format("{notification:{title:\"%s\",body:\"Push message send from Comapi\",channelId:\"id\"},messageId:\"%s\",actions:[{link:\"%s\",action:\"%s\",id:\"%s\"}]}", title, messageId, link, action, actionId));

        Method method = receiver.getClass().getDeclaredMethod("handleData", Map.class);
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
