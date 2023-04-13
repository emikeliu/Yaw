package zq.yaw.utils;

import android.graphics.Bitmap;


public class PropertiesPerSite {
    public boolean noMoreAlert;
    public boolean noMoreOpen;
    public Bitmap icon;
    public int jsTimes = 0;
    public long lastTime = 0;
    public String title;

    {
        reset();
    }

    public void reset() {
        noMoreAlert = false;
        noMoreOpen = false;
        icon = null;
        jsTimes = 0;
        lastTime = 0;
        title = null;
    }
}
