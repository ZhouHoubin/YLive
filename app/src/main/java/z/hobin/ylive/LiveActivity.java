package z.hobin.ylive;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import z.hobin.ylive.util.ACache;
import z.hobin.ylive.util.ScreenSwitchUtils;
import z.hobin.ylive.util.WindowUtil;

public class LiveActivity extends BaseActivity implements View.OnClickListener {
    protected ScreenSwitchUtils switchUtils;
    private JSONObject json;
    //房间ID
    private String roomId = null;
    //房间地址
    private String roomUrl = null;
    private String h5RoomUrl = null;
    //画质
    protected int rateIndex = 1;
    //线路
    protected int lineIndex = 0;
    protected ExtractorsFactory extractorsFactory;
    protected DataSource.Factory dataSourceFactory;
    protected ExoPlayer player;
    private SimpleExoPlayerView videoView;
    protected View liveInfo;
    //pc webview
    protected WebView liveWeb;
    //mobile webview
    protected WebView liveMobileWeb;
    //画质列表
    private List<RateInfo> multiRateInfo = new ArrayList<>();
    //线路列表
    private List<LineInfo> multiLineInfo = new ArrayList<>();
    //直播信息
    private HuyaLiveInfo huyaLiveInfo = null;
    private Handler handler = new Handler();

    protected DanmakuView danmakuView;
    private DanmakuContext danmakuContext;
    protected TextView btnLine;
    protected TextView btnRate;
    protected TextView btnRefersh;
    protected TextView btnFull;
    protected TextView btnDanMu;
    //弹幕选择
    private int danmuFlag = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_live);

        switchUtils = ScreenSwitchUtils.init(this.getApplicationContext());

        videoView = findViewById(R.id.videoView);
        danmakuView = findViewById(R.id.danmaku_view);
        videoView.setKeepScreenOn(true);

        liveInfo = findViewById(R.id.live_info);

        player = creteAPlayer(videoView);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "yourApplicationName"), bandwidthMeter);
        extractorsFactory = new DefaultExtractorsFactory();

        liveWeb = findViewById(R.id.live_web);
        liveMobileWeb = findViewById(R.id.live_web_mobile);
        WebSettings mobileSettings = liveMobileWeb.getSettings();
        liveMobileWeb.setWebViewClient(new WebViewClient());
        liveMobileWeb.setWebChromeClient(new WebChromeClient());

        mobileSettings.setJavaScriptEnabled(true);
        mobileSettings.setDatabaseEnabled(true);
        mobileSettings.setDomStorageEnabled(true);
        mobileSettings.setAppCacheEnabled(true);


        WebSettings webSettings = liveWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");

        liveWeb.setWebViewClient(new WebViewClient());
        liveWeb.setWebChromeClient(new WebChromeClient());

        liveMobileWeb.setWebContentsDebuggingEnabled(true);

        btnFull = videoView.findViewById(R.id.exo_full);
        btnDanMu = videoView.findViewById(R.id.exo_danmu);
        btnRefersh = videoView.findViewById(R.id.exo_refersh);
        btnRate = videoView.findViewById(R.id.exo_rate);
        btnLine = videoView.findViewById(R.id.exo_line);

        btnRefersh.setOnClickListener(this);
        btnFull.setOnClickListener(this);
        btnRate.setOnClickListener(this);
        btnLine.setOnClickListener(this);
        btnDanMu.setOnClickListener(this);

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!switchUtils.isPortrait()) {
                    WindowUtil.setFullScreen(LiveActivity.this);
                }
                return false;
            }
        });

        initDanmaKu();
    }

    private void initDanmaKu() {
        danmakuContext = DanmakuContext.create();
        // 设置最大行数,从右向左滚动(有其它方向可选)
        HashMap maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3);

        // 设置是否禁止重叠
        HashMap overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);

        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE, 3) //设置描边样式
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.5f)
                .setDanmakuMargin(10)
                .setScaleTextSize(1.2f) //设置弹幕滚动速度系数,只对滚动弹幕有效
                // 默认使用{@link SimpleTextCacheStuffer}只支持纯文字显示,
                // 如果需要图文混排请设置{@link SpannedCacheStuffer}
                // 如果需要定制其他样式请扩展{@link SimpleTextCacheStuffer}|{@link SpannedCacheStuffer}
                .setMaximumLines(maxLinesPair) //设置最大显示行数
                .preventOverlapping(overlappingEnablePair); //设置防弹幕重叠，null为允许重叠
        danmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
            @Override
            public void updateTimer(DanmakuTimer timer) {
            }

            @Override
            public void drawingFinished() {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void prepared() {
                danmakuView.start();
            }
        });
        danmakuView.prepare(new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        }, danmakuContext);

        String value = cache.getAsString("danmu");
        if (!TextUtils.isEmpty(value)) {
            danmuFlag = Integer.parseInt(value);
        }
        switch (danmuFlag) {
            case 0:
                danmakuView.show();
                danmakuView.setAlpha(1f);
                break;
            case 1:
                danmakuView.hide();
                danmakuView.setAlpha(1f);
                break;
            case 2:
                danmakuView.setAlpha(0.25f);
                break;
            case 3:
                danmakuView.setAlpha(0.50f);
                break;
            case 4:
                danmakuView.setAlpha(0.75f);
                break;
            default:
                break;
        }
    }

    /**
     * 添加文本弹幕
     *
     * @param text
     */
    protected void addDanmaku(String text) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || danmakuView == null) {
            return;
        }
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.priority = 1;  //0 表示可能会被各种过滤器过滤并隐藏显示 //1 表示一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = true; //是否是直播弹幕
        danmaku.setTime(danmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 30f;
        danmaku.textColor = Color.WHITE;
        danmaku.textShadowColor = Color.BLACK; //阴影/描边颜色
        danmaku.borderColor = 0; //边框颜色，0表示无边框
        danmakuView.addDanmaku(danmaku);
        if (switchUtils.isPortrait()) {
            danmakuView.hide();
        } else if (danmuFlag != 1) {
            danmakuView.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exo_line:
                break;
            case R.id.exo_rate:
                break;
            case R.id.exo_full:
                if (switchUtils.isPortrait()) {
                    switchUtils.toggleScreen();
                } else {
                    switchUtils.toggleScreen();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                break;
            case R.id.exo_refersh:
                play(lineIndex, rateIndex);
                break;
            case R.id.exo_danmu:
                AlertDialog.Builder danmuBuilder = new AlertDialog.Builder(LiveActivity.this);
                CharSequence[] danmuItems = new CharSequence[]{"开启弹幕", "关闭弹幕", "透明度25%", "透明度50%", "透明度75%"};
                danmuBuilder.setSingleChoiceItems(danmuItems, danmuFlag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        danmuFlag = which;
                        cache.put("danmu", String.valueOf(danmuFlag));
                        switch (which) {
                            case 0:
                                danmakuView.show();
                                danmakuView.setAlpha(1f);
                                break;
                            case 1:
                                danmakuView.hide();
                                danmakuView.setAlpha(1f);
                                break;
                            case 2:
                                danmakuView.setAlpha(0.25f);
                                break;
                            case 3:
                                danmakuView.setAlpha(0.50f);
                                break;
                            case 4:
                                danmakuView.setAlpha(0.75f);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                danmuBuilder.show();
                break;
            default:
                break;
        }
    }

    protected void play(int line, int rate) {

    }

    protected ExoPlayer creteAPlayer(SimpleExoPlayerView view) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
        view.setPlayer(player);
        return player;
    }

    protected class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            LiveActivity.this.onConsoleMessage(consoleMessage);
            return super.onConsoleMessage(consoleMessage);
        }
    }

    protected void onConsoleMessage(ConsoleMessage consoleMessage) {
        String message = consoleMessage.message();
        if (message.startsWith("[") && message.endsWith("]")) {
            message = message.replace("[", "");
            message = message.replace("]", "");
            if (message.contains(":")) {
                message = message.replace(":", ": ");
                addDanmaku(message);
            }
        }
    }

    protected class WebViewClient extends android.webkit.WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!switchUtils.isPortrait()) {
            switchUtils.toggleScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchUtils.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        switchUtils.stop();
        player.setPlayWhenReady(false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        if (danmakuView != null) {
            // dont forget release!
            danmakuView.release();
            danmakuView = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (switchUtils.isPortrait()) {
            danmakuView.hide();
            btnDanMu.setVisibility(View.GONE);
            liveInfo.setVisibility(View.VISIBLE);
            videoView.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp_200)));
            WindowUtil.cancelFullScreen(this);
        } else {
            danmakuView.show();
            btnDanMu.setVisibility(View.VISIBLE);
            liveInfo.setVisibility(View.GONE);
            videoView.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            WindowUtil.setFullScreen(this);
        }
    }
}
