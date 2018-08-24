package z.hobin.ylive.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class LiveWebView extends WebView {
    public LiveWebView(Context context) {
        super(context);
    }

    public LiveWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
