package com.shubham.ambientlight;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. सिंटैक्स एरर ठीक किया (super.override को super.onCreate किया)
        super.onCreate(savedInstanceState);
        
        // नोट: अगर आपके पास activity_main.xml लेआउट है, तो आप नीचे वाली लाइन को अनकमेंट (Uncomment) कर सकते हैं
        // setContentView(R.layout.activity_main);

        // ओवरले परमिशन चेक करने का लॉजिक
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            startAmbientService();
        }
    }

    private void startAmbientService() {
        Intent serviceIntent = new Intent(this, AmbientService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        finish(); // सर्विस स्टार्ट होने के बाद एक्टिविटी बंद करना
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startAmbientService();
            } else {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
