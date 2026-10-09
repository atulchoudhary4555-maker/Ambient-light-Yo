package com.shubham.ambientlight;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.core.app.NotificationCompat;

public class AmbientService extends Service {
    private WindowManager windowManager;
    private FrameLayout overlayView;
    private static final String CHANNEL_ID = "AmbientLightChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        // एंड्रॉइड 14+ के नियमों के तहत सर्विस को फ़ोरग्राउंड में स्टार्ट करना
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ambient Light Active")
                .setContentText("Ambient lighting overlay is running in the background")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
                
        startForeground(NOTIFICATION_ID, notification);
        
        // स्क्रीन के ऊपर एम्बिएंट लाइट (Overlay) दिखाने का लॉजिक
        showAmbientOverlay();
    }

    private void showAmbientOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // एक खाली लेआउट बनाना जो पूरी स्क्रीन को कवर करे
        overlayView = new FrameLayout(this);
        
        // यहाँ आप अपनी पसंद का कलर सेट कर सकते हैं (उदाहरण के लिए हल्का पारदर्शी लाल बॉर्डर/लाइट)
        overlayView.setBackgroundColor(Color.argb(50, 255, 0, 0)); 

        // एंड्रॉइड के वर्ज़न के हिसाब से विंडो टाइप सेट करना
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        windowManager.addView(overlayView, params);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ambient Light Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // ऐप बंद होने पर स्क्रीन से ओवरले हटाना ताकि मेमोरी लीक न हो
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
    }
                         
