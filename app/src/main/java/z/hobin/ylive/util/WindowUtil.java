package z.hobin.ylive.util;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class WindowUtil {
    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void setNoTitleBar(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void hideActionBar(AppCompatActivity activity) {
        activity.getSupportActionBar().hide();
    }

    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void showActionBar(AppCompatActivity activity) {
        activity.getSupportActionBar().show();
    }

    /**
     * 设置全屏
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideActionBar((AppCompatActivity) activity);
    }

    /**
     * 取消全屏
     *
     * @param activity
     */
    public static void cancelFullScreen(Activity activity) {
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        showActionBar((AppCompatActivity) activity);
    }
}
