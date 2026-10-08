package com.shubham.ambientlight;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int REQ_CAPTURE = 2001;
    private SeekBar strength;
    private TextView status;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setPadding(40,50,40,40);
        TextView title = new TextView(this); title.setText("Ambient Light for YouTube"); title.setTextSize(24); box.addView(title);
        TextView info = new TextView(this); info.setText("YouTube video ke colors se screen ke edges par soft glow. Start dabakar permissions allow karein."); box.addView(info);
        strength = new SeekBar(this); strength.setMax(100); strength.setProgress(65); box.addView(strength);
        Button start = new Button(this); start.setText("START AMBIENT LIGHT"); box.addView(start);
        Button stop = new Button(this); stop.setText("STOP"); box.addView(stop);
        status = new TextView(this); status.setText("Status: Stopped"); box.addView(status);
        setContentView(box);
        start.setOnClickListener(v -> startAmbient());
        stop.setOnClickListener(v -> { stopService(new Intent(this, AmbientService.class)); status.setText("Status: Stopped"); });
    }
    private void startAmbient() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            status.setText("Overlay permission ON karke START dobara dabayein."); return;
        }
        MediaProjectionManager m = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(m.createScreenCaptureIntent(), REQ_CAPTURE);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAPTURE && resultCode == RESULT_OK && data != null) {
            Intent s = new Intent(this, AmbientService.class);
            s.putExtra("code", resultCode); s.putExtra("data", data); s.putExtra("strength", strength.getProgress());
            startService(s); status.setText("Status: Running");
        }
    }
}
