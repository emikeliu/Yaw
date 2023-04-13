package zq.yaw.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class SafeEditText extends androidx.appcompat.widget.AppCompatEditText {
    private OnEditTextKeyBackListener listener;

    public SafeEditText(Context context) {
        super(context);
    }

    public SafeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public SafeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null) {
                listener.onKeyBack();

                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnEditTextKeyBackListener(OnEditTextKeyBackListener listener) {
        this.listener = listener;
    }

    public interface OnEditTextKeyBackListener {
        void onKeyBack();
    }
}
