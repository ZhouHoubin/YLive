package z.hobin.ylive.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        if(activity.getSupportActionBar() != null){
            activity.getSupportActionBar().hide();
        }
    }

    /**
     * 设置隐藏标题栏
     *
     * @param activity
     */
    public static void showActionBar(AppCompatActivity activity) {
        if(activity.getSupportActionBar() != null){
            activity.getSupportActionBar().show();
        }
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
        //hideNavi(activity);
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
        //showNavi(activity);
    }

    public static void showNavi(Activity activity){
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    public static void hideNavi(Activity activity){
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
