package com.shubham.ambientlight;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // एक मेन लेआउट बनाना
        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setBackgroundColor(Color.BLACK); // बैकग्राउंड काला रहेगा ताकि लाइट अच्छी दिखे

        // वेबव्यू (WebView) सेटअप करना जिसमें यूट्यूब चलेगा
        webView = new WebView(this);
        FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        mainLayout.addView(webView, webViewParams);
        setContentView(mainLayout);

        // वेबव्यू सेटिंग्स (जावास्क्रिप्ट ऑन करना ज़रूरी है)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // जैसे ही यूट्यूब लोड होगा, यह स्क्रिप्ट वीडियो के पीछे एक ग्लो (Ambient Light) इफेक्ट बना देगी
                injectAmbientLightScript();
            }
        });

        // मोबाइल यूट्यूब वेबसाइट लोड करना
        webView.loadUrl("https://youtube.com");
    }

    private void injectAmbientLightScript() {
        // यह जादुई कोड यूट्यूब वीडियो के रंगों को स्क्रीन के बैकग्राउंड पर ग्लो के रूप में फैलाता है
        String jsCode = "javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'video { box-shadow: 0px 0px 80px 40px rgba(255,255,255,0.6); transition: box-shadow 0.3s ease; }';" +
                "document.head.appendChild(style);" +
                "var video = document.querySelector('video');" +
                "if(video) {" +
                "    video.addEventListener('play', function() {" +
                "        setInterval(function() {" +
                "            var canvas = document.createElement('canvas');" +
                "            canvas.width = 1; canvas.height = 1;" +
                "            var ctx = canvas.getContext('2d');" +
                "            ctx.drawImage(video, 0, 0, 1, 1);" +
                "            var c = ctx.getImageData(0, 0, 1, 1).data;" +
                "            video.style.boxShadow = '0px 0px 100px 50px rgba('+c[0]+','+c[1]+','+c[2]+',0.8)';" +
                "        }, 300);" +
                "    });" +
                "}" +
                "})()";
        webView.evaluateJavascript(jsCode, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack(); // पीछे जाने पर यूट्यूब का पिछला पेज खुलेगा, ऐप बंद नहीं होगी
        } else {
            super.onBackPressed();
        }
    }
}
