package com.ashwithkumar.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class ClockActivity extends Activity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));
    }

    private Bitmap screensaverBitmap;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dream_clock);
        ClockFormat.apply(getWindow().getDecorView(), Config.load(this).clockFormat);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        ImageView imageView = findViewById(R.id.dreamAmbient);
        File screensaverFile = findScreensaverFile();
        if (screensaverFile != null && screensaverFile.exists()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            screensaverBitmap = BitmapFactory.decodeFile(screensaverFile.getAbsolutePath(), opts);
            if (screensaverBitmap != null) {
                imageView.setImageBitmap(screensaverBitmap);
            }
        }
    }

    private File findScreensaverFile() {
        File dir = getExternalFilesDir(null);
        String[] names = {"screensaver.png", "screensaver.jpg", "wallpaper.png", "wallpaper.jpg"};
        if (dir != null) {
            for (String n : names) {
                File f = new File(dir, n);
                if (f.isFile() && f.length() > 0) return f;
            }
        }
        File tmp = new File("/data/local/tmp/screensaver.png");
        return tmp.isFile() && tmp.length() > 0 ? tmp : null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (screensaverBitmap != null && !screensaverBitmap.isRecycled()) {
            screensaverBitmap.recycle();
            screensaverBitmap = null;
        }
    }

    private String appPackageForKey(int code) {
        switch (code) {
            case 284: return "com.netflix.ninja";
            case 285: return "com.google.android.youtube.tv";
            case 286: return "com.amazon.amazonvideo.livingroom";
            case 287: return "in.startv.hotstar";
            case 288: return "com.sonyliv";
            case KeyEvent.KEYCODE_TV:
            case KeyEvent.KEYCODE_GUIDE:
                return Config.load(this).liveTvPackage;
            default:
                return null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN
                || code == KeyEvent.KEYCODE_VOLUME_MUTE || code == KeyEvent.KEYCODE_POWER) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String pkg = appPackageForKey(code);
            if (pkg != null) {
                Intent intent = getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
                if (intent == null) intent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (intent != null) {
                    try { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception ignored) {}
                }
            }
            finish();
        }
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
