package com.comapi.internal.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import com.comapi.internal.receivers.NotificationClickReceiver;

import java.util.Map;
import java.util.Random;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public
class PushBuilder {

    private final String correlationId;
    private final String title;
    private final String body;
    private final String url;

    public PushBuilder(String correlationId, String title, String body, String url) {
        this.correlationId = correlationId;
        this.title = title;
        this.body = body;
        this.url = url;
    }

    private void setupChannel(Context context, ChannelData channelData) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(channelData.getId(), channelData.getName(), NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(channelData.getDescription());
                channel.enableLights(true);
                channel.setLightColor(Color.WHITE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0L, 100L});
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    Notification buildNotification(Context context, ChannelData channelData, PushUIConfig pushUIConfig) {

        setupChannel(context, channelData);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelData.getId())
                        .setSmallIcon(pushUIConfig.getSmallIconId())
                        .setContentTitle(title)
                        .setContentText(Html.fromHtml(handleEmptyMessage(body)))
                        .setVibrate(pushUIConfig.getVibratePattern())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(handleEmptyMessage(body))).setBigContentTitle(title))
                        .setCategory(pushUIConfig.getCategory())
                        .setPriority(pushUIConfig.getPriority())
                        .setVisibility(pushUIConfig.getVisibility())
                        .setSound(pushUIConfig.getSoundUri())
                        .setColor(pushUIConfig.getIconBackgroundColor())
                        .setTicker(title);

        //if (avatar != null) {
        //  builder.setLargeIcon(avatar);
        //}

        builder.setContentIntent(createPendingIntent(context));
        builder.setAutoCancel(true);

        Notification notification = builder.build();

        notification.ledARGB = context.getResources().getColor(pushUIConfig.getLedColour());
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = pushUIConfig.getLedOnMilliseconds();
        notification.ledOffMS = pushUIConfig.getLedOffMilliseconds();

        return notification;
    }

    private PendingIntent createPendingIntent(Context context) {

        Intent intent = new Intent(context, NotificationClickReceiver.class);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(PushDataKeys.PUSH_CLICK_ACTION);
        intent.putExtra(PushDataKeys.KEY_PUSH_DEEP_LINK, url);
        intent.putExtra(PushDataKeys.KEY_PUSH_ACTION_ID, "TODO");
        intent.putExtra(PushDataKeys.KEY_PUSH_CORRELATION_ID, correlationId);
        int pendingIntentId = Math.abs(new Random().nextInt(Integer.MAX_VALUE));
        return PendingIntent.getBroadcast(context, pendingIntentId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns a non-empty value of message
     *
     * @param message The message
     * @return The new message
     */
    private String handleEmptyMessage(String message) {

        if (TextUtils.isEmpty(message)) {
            return "<i></i>";
        } else {
            return message;
        }

    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getClickActionDetails() {
        return null;
    }
}
