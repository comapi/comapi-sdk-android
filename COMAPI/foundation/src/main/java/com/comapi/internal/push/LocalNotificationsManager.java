package com.comapi.internal.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.io.Serializable;

import com.comapi.R;
import com.comapi.internal.log.Logger;
import com.comapi.internal.network.InternalService;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Marcin Swierczek
 * @since 1.4.0
 */
public class LocalNotificationsManager {

    private WeakReference<Context> context;
    private WeakReference<InternalService> service;

    private ChannelData channelData;
    private PushUIConfig uiConfig;
    private Logger log;

    LocalNotificationsManager(Context context, Logger log) {
        this.context = new WeakReference<>(context);
        channelData = new ChannelData(context.getString(R.string.comapi_default_channel_id),
                context.getString(R.string.comapi_default_channel_name),
                context.getString(R.string.comapi_default_channel_description));
        uiConfig = new PushUIConfig(context);
        this.log = log;
    }

    public void handleNotification(PushBuilder builder) {

        Context context = this.context.get();
        if (context != null) {
            int id = UUID.randomUUID().hashCode();
            Notification n = builder.buildNotification(context, channelData, uiConfig);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(id, n);
            } else {
                log.e("NotificationManager unavailable in LocalNotificationsManager.handleNotification");
            }
        } else {
            log.e("Missing week context reference in LocalNotificationsManager.handleNotification");
        }
    }

    public void handleNotificationClick(String trackingUrl, Serializable data, String link) {

        if (trackingUrl != null) {
            InternalService service = this.service.get();
            if (service != null) {
                callObs(service.sendClickData(trackingUrl));
            } else {
                log.e("Missing week reference to InternalService in LocalNotificationsManager.handleNotification");
            }
        }

        if (link != null) {
            try {
                Intent intent = createDeepLinkIntent(trackingUrl, data, link);
                Context context = this.context.get();
                if (context != null) {
                    if (isActivityAvailable(context, intent)) {
                        context.getApplicationContext().startActivity(intent);
                    }
                } else {
                    log.e("Missing week context reference in LocalNotificationsManager.handleNotification");
                }
            } catch (Exception e) {
                log.f("Error creating deep link intent trackingUrl="+trackingUrl+" link="+link, e);
            }
        }
    }

    public void setService(InternalService service) {
        this.service = new WeakReference<InternalService>(service);
    }

    private <T> void callObs(Observable<T> o) {
        o.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<T>() {

                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        log.e(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(T result) {}
                });
    }

    private Intent createDeepLinkIntent(String trackingUrl, Serializable data, String link) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(link));
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (data instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> map = ((HashMap<String, String>) data);
            for (Map.Entry<String, String> pair : map.entrySet()) {
                intent.putExtra(pair.getKey(), pair.getValue());
            }
        } else if (data != null) {
            intent.putExtra(PushDataKeys.KEY_PUSH_DATA, data);
        }
        if (trackingUrl != null) {
            intent.putExtra(PushDataKeys.KEY_PUSH_TRACKING_URL, trackingUrl);
        }
        return intent;
    }

    private boolean isActivityAvailable(Context context, Intent intent) {
            final PackageManager mgr = context.getApplicationContext().getPackageManager();
            List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
    }
}
