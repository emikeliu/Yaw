package zq.yaw.utils;

import android.os.Binder;

public class StringBinder extends Binder {
    public String trans;

    public StringBinder setString(String str) {
        this.trans = str;
        return this;
    }
}
