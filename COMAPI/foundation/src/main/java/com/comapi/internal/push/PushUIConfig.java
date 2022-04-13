package com.comapi.internal.push;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.comapi.R;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public class PushUIConfig {

    private int ledColour;

    private int ledOnMilliseconds;

    private int ledOffMilliseconds;

    private PendingIntent contentIntent;

    private Uri soundUri;

    private long[] vibratePattern;

    private String category;

    private int priority;

    private int visibility;

    private int iconBackgroundColor;

    private int[] actionButtonIconIds;

    private PendingIntent[] pendingIntents;

    private int smallIconId;

    private String pushChannelName;

    private String pushChannelDescription;

    private int pushChannelImportance;

    @SuppressLint("NewApi")
    public PushUIConfig(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.category = Notification.CATEGORY_MESSAGE;
            this.priority = Notification.PRIORITY_HIGH;
            this.visibility = Notification.VISIBILITY_PUBLIC;
        }
        this.vibratePattern = new long[] {0L, 100L};
        this.soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        this.iconBackgroundColor = R.color.comapi_notification_icon_background;
        this.ledOnMilliseconds = 200;
        this.ledOffMilliseconds = 2000;
        this.ledColour = R.color.comapi_notification_led;
        this.actionButtonIconIds = new int[] { android.R.color.transparent, android.R.color.transparent };
        this.pendingIntents = new PendingIntent[] {null, null};

        int attributeResourceId = -1;
        try {
            String packageName = context.getApplicationContext().getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            int theme = packageInfo.applicationInfo.theme;
            int[] attribute = new int[] {R.attr.comapi_notification_small_icon};
            TypedArray array = context.getTheme().obtainStyledAttributes(theme, attribute);
            attributeResourceId = array.getResourceId(0, -1);
            array.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            // Error getting styled notification icon"
        }

        if (attributeResourceId != -1) {
            this.smallIconId = attributeResourceId;
        } else {
            this.smallIconId = R.drawable.comapi_notification_small_icon;
        }

        pushChannelName = "Push notifications";
        pushChannelDescription = "Text notifications for the app";
        pushChannelImportance = NotificationManager.IMPORTANCE_HIGH;
    }

    /**
     * Small icon for system notification.
     *
     * @return Small icon for system notification.
     */
    public int getSmallIconId() {
        return smallIconId;
    }

    /**
     * Get sound for notification.
     *
     * @return Sound for notification.
     */
    public Uri getSoundUri() {
        return soundUri;
    }

    /**
     * Set sound for notification.
     *
     * @param soundUri Sound for notification.
     */
    public void setSoundUri(Uri soundUri) {
        this.soundUri = soundUri;
    }

    /**
     * Get vibration pattern for notification.
     *
     * @return Vibration pattern for notification.
     */
    public long[] getVibratePattern() {
        return vibratePattern;
    }

    /**
     * Set vibration pattern for notification.
     *
     * @param vibratePattern Vibration pattern for notification.
     */
    public void setVibratePattern(long[] vibratePattern) {
        this.vibratePattern = vibratePattern;
    }

    /**
     * Get notification category. Category is interpreted by OS.
     *
     * @return Notification category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set notification category. Category is interpreted by OS.
     *
     * @param category Notification category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get notification priority. Priority is interpreted by OS.
     *
     * @return Notification priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set notification priority. Priority is interpreted by OS.
     *
     * @param priority Notification priority.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get notification visibility. Visibility is interpreted by OS.
     *
     * @return Notification visibility.
     */
    public int getVisibility() {
        return visibility;
    }

    /**
     * Set notification visibility. Visibility is interpreted by OS.
     *
     * @param visibility Notification visibility.
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /**
     * Get background color for small icon if large icon is not available.
     *
     * @return Background color for small icon if large icon is not available.
     */
    public int getIconBackgroundColor() {
        return iconBackgroundColor;
    }

    /**
     * Set background color for small icon if large icon is not available.
     *
     * @param iconBackgroundColor Background color for small icon if large icon is not available.
     */
    public void setIconBackgroundColor(int iconBackgroundColor) {
        this.iconBackgroundColor = iconBackgroundColor;
    }

    /**
     * Get led color.
     *
     * @return Led color.
     */
    public int getLedColour() {
        return ledColour;
    }

    /**
     * Get time period when led light will be switched on after notification was received.
     *
     * @return Time period when led light will be switched on after notification was received.
     */
    public int getLedOnMilliseconds() {
        return ledOnMilliseconds;
    }

    /**
     * Get time period when led light will be switched off after notification was received.
     *
     * @return Time period when led light will be switched off after notification was received.
     */
    public int getLedOffMilliseconds() {
        return ledOffMilliseconds;
    }

    /**
     * Set colour of led light.
     *
     * @param ledColour Colour of led light.
     */
    public void setLedColour(int ledColour) {
        this.ledColour = ledColour;
    }

    /**
     * Set time period when led light will be switched on after notification was received.
     *
     * @param ledOnMilliseconds Time period when led light will be switched on after notification was received.
     */
    public void setLedOnMilliseconds(int ledOnMilliseconds) {
        this.ledOnMilliseconds = ledOnMilliseconds;
    }

    /**
     * Set time period when led light will be switched off after notification was received.
     *
     * @param ledOffMilliseconds Time period when led light will be switched off after notification was received.
     */
    public void setLedOffMilliseconds(int ledOffMilliseconds) {
        this.ledOffMilliseconds = ledOffMilliseconds;
    }

    /**
     * Get button icon ids for interactive simple push.
     *
     * @return Button icon ids.
     */
    public int[] getActionButtonIconIds() {
        return actionButtonIconIds;
    }

    /**
     * Set button icon ids for interactive simple push.
     *
     * @param actionButtonIconIds Button icon ids.
     */
    public void setActionButtonIconIds(int[] actionButtonIconIds) {
        this.actionButtonIconIds = actionButtonIconIds;
    }

    /**
     * Get PendingIntents defining actions for notification buttons.
     *
     * @return PendingIntents defining actions for notification buttons.
     */
    public PendingIntent[] getPendingIntents() {
        return pendingIntents;
    }

    /**
     * Set PendingIntents defining actions for notification buttons.
     *
     * @param first PendingIntent defining actions for notification first button.
     * @param second PendingIntent defining actions for notification second button.
     */
    public void setPendingIntents(PendingIntent first, PendingIntent second) {

        this.pendingIntents = new PendingIntent[] {first, second};

    }

    /**
     * Gets push channel name.
     *
     * @return push channel name.
     */
    public String getPushChannelName() {
        return pushChannelName;
    }

    /**
     * Gets push channel description.
     *
     * @return push channel description.
     */
    public String getPushChannelDescription() {
        return pushChannelDescription;
    }

    /**
     * Sets push channel name.
     *
     * @param pushChannelName Push channel name.
     */
    public void setPushChannelName(String pushChannelName) {
        this.pushChannelName = pushChannelName;
    }

    /**
     * Sets push channel description.
     *
     * @param pushChannelDescription push channel description.
     */
    public void setPushChannelDescription(String pushChannelDescription) {
        this.pushChannelDescription = pushChannelDescription;
    }

    /**
     * Gets push channel importance.
     *
     * @return Push channel description.
     */
    public int getPushChannelImportance() {
        return pushChannelImportance;
    }

    /**
     * Sets push channel importance.
     *
     * @param pushChannelImportance Push channel importance.
     */
    public void setPushChannelImportance(int pushChannelImportance) {
        this.pushChannelImportance = pushChannelImportance;
    }
}
