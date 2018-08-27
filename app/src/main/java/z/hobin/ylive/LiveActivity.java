package z.hobin.ylive;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import z.hobin.ylive.util.CircleImageTransformation;
import z.hobin.ylive.util.ScreenSwitchUtils;
import z.hobin.ylive.util.WindowUtil;

public class LiveActivity extends AppCompatActivity implements View.OnClickListener {
    private ScreenSwitchUtils instance;
    private JSONObject json;
    //房间ID
    private String roomId = null;
    //房间地址
    private String roomUrl = null;
    private String h5RoomUrl = null;
    //画质
    private int rateIndex = 1;
    //线路
    private int lineIndex = 0;
    private ExtractorsFactory extractorsFactory;
    private DataSource.Factory dataSourceFactory;
    private ExoPlayer player;
    private SimpleExoPlayerView videoView;
    private View liveInfo;
    private WebView liveWeb;
    private WebView liveMobileWeb;
    private Huya huya;
    //画质列表
    private List<StreamInfo> multiRateInfo = new ArrayList<>();
    //线路列表
    private List<LineInfo> multiLineInfo = new ArrayList<>();
    //直播信息
    private HuyaLiveInfo huyaLiveInfo = null;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_live);

        instance = ScreenSwitchUtils.init(this.getApplicationContext());

        String data = getIntent().getStringExtra("data");
        try {
            json = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        videoView = findViewById(R.id.videoView);
        videoView.setKeepScreenOn(true);

        liveInfo = findViewById(R.id.live_info);

        try {
            TextView liveTitle = liveInfo.findViewById(R.id.live_title);
            liveTitle.setText(json.getString("introduction"));
            TextView liveUser = liveInfo.findViewById(R.id.live_user);
            liveUser.setText(json.getString("nick"));
            ImageView liveAvatar = liveInfo.findViewById(R.id.live_avatar);
            Picasso.get().load(json.getString("avatar180")).transform(new CircleImageTransformation()).into(liveAvatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        try {
            roomId = json.getString("profileRoom");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        roomUrl = "http://www.huya.com/" + roomId;
        h5RoomUrl = "http://m.huya.com/" + roomId;
        huya = new Huya();
        liveWeb.loadUrl(roomUrl);
        liveMobileWeb.loadUrl(h5RoomUrl);
        liveMobileWeb.setWebContentsDebuggingEnabled(true);

        videoView.findViewById(R.id.exo_refersh).setOnClickListener(this);
        videoView.findViewById(R.id.exo_full).setOnClickListener(this);
        videoView.findViewById(R.id.exo_rate).setOnClickListener(this);
        videoView.findViewById(R.id.exo_line).setOnClickListener(this);

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!instance.isPortrait()) {
                    WindowUtil.setFullScreen(LiveActivity.this);
                }
                return false;
            }
        });
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                System.out.println("LiveActivity.onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                System.out.println("LiveActivity.onTracksChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                System.out.println("LiveActivity.onLoadingChanged " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                System.out.println("LiveActivity.onPlayerError");
                Toast.makeText(LiveActivity.this, "播放错误，请切换线路或者修改画质", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPositionDiscontinuity() {
                System.out.println("LiveActivity.onPositionDiscontinuity");
            }
        });

        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
        //startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exo_line:
                AlertDialog.Builder lineBuilder = new AlertDialog.Builder(LiveActivity.this);
                CharSequence[] lineItems = new CharSequence[multiLineInfo.size()];
                for (int i = 0; i < multiLineInfo.size(); i++) {
                    LineInfo lineInfo = multiLineInfo.get(i);
                    lineItems[i] = lineInfo.title;
                }
                lineBuilder.setSingleChoiceItems(lineItems, lineIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                lineIndex = which;
                                play(lineIndex, rateIndex);
                            }
                        }.start();
                        dialog.dismiss();
                    }
                });
                lineBuilder.show();
                break;
            case R.id.exo_rate:
                AlertDialog.Builder builder = new AlertDialog.Builder(LiveActivity.this);
                CharSequence[] items = new CharSequence[multiRateInfo.size()];
                for (int i = 0; i < multiRateInfo.size(); i++) {
                    StreamInfo streamInfo = multiRateInfo.get(i);
                    items[i] = streamInfo.name;
                }
                builder.setSingleChoiceItems(items, rateIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                rateIndex = which;
                                play(lineIndex, rateIndex);
                            }
                        }.start();
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.exo_full:
                if (instance.isPortrait()) {
                    instance.toggleScreen();
                } else {
                    instance.toggleScreen();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                break;
            case R.id.exo_refersh:
                play(lineIndex, rateIndex);
                break;
        }
    }

    private void play(int line, int rate) {
        String url = multiLineInfo.get(line).url;
        if (rate != 0) {
            url += "&ratio=" + multiRateInfo.get(rate).rate;
        }
        url += "&u=-1338516808&t=100&sv=1808231100";
        System.out.println("===================\r\n" + url);
        String finalUrl = url;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse(finalUrl), dataSourceFactory, extractorsFactory, null, null);
                player.prepare(videoSource);
                player.setPlayWhenReady(true);
            }
        });
    }

    private ExoPlayer creteAPlayer(SimpleExoPlayerView view) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector, loadControl);
        view.setPlayer(player);
        return player;
    }

    private class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            System.out.println(consoleMessage.lineNumber() + "::" + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private class WebViewClient extends android.webkit.WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("m.huya.com")) {
                view.evaluateJavascript("$('#chatArea').css('width','100%');$('#chatArea').css('height','100%');$('#chatArea').css('top','0');$('#chatArea').css('position','fixed');$('.huya-header').css('display','none');$('.live-wrap').css('display','none');$('.live-info-btn').css('display','none');$('#m-container').attr('style','padding-top:0;');$('#m-container > div.live_tab').css('height','0');", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                view.setVisibility(View.VISIBLE);
                            }
                        }, 1000);
                    }
                });
            } else {
                huya.load(view, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        multiRateInfo = huya.getMultiStreamInfo(value);
                        multiLineInfo = huya.getMultiLineInfo(value);
                        huyaLiveInfo = huya.getLiveInfo(value);
                        play(0, 1);
                    }
                });
            }
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
        if (!instance.isPortrait()) {
            instance.toggleScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(player != null){
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        instance.stop();
        player.setPlayWhenReady(false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (instance.isPortrait()) {
            liveInfo.setVisibility(View.VISIBLE);
            videoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.dp_200)));
            WindowUtil.cancelFullScreen(this);
        } else {
            liveInfo.setVisibility(View.GONE);
            videoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            WindowUtil.setFullScreen(this);
        }
    }
}
