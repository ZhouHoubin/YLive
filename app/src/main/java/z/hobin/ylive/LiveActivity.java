package z.hobin.ylive;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import z.hobin.ylive.util.ScreenSwitchUtils;
import z.hobin.ylive.util.WindowUtil;

public class LiveActivity extends AppCompatActivity {
    private ScreenSwitchUtils instance;
    private JSONObject json;
    private String roomId = null;
    private int streamLine = 0;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    private ExtractorsFactory extractorsFactory;
    private DataSource.Factory dataSourceFactory;
    private ExoPlayer player;
    private SimpleExoPlayerView videoView;
    private View liveInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        setContentView(R.layout.activity_live);

        instance = ScreenSwitchUtils.init(this.getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String data = getIntent().getStringExtra("data");
        try {
            json = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        videoView = findViewById(R.id.videoView);
        videoView.setKeepScreenOn(true);

        liveInfo = findViewById(R.id.live_info);

        player = creteAPlayer(videoView);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "yourApplicationName"), bandwidthMeter);
        extractorsFactory = new DefaultExtractorsFactory();

        if (json == null) {
            return;
        } else {
            try {
                getSupportActionBar().setTitle(json.getString("nick"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                getSupportActionBar().setSubtitle(json.getString("roomName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    load(json);
                }
            }.start();
        }

        String[] urls = new String[]{
                "http://ws4.stream.huya.com/hqlive/95431869-2562758913-11006965718867509248-34850612-10057-A-0-1.flv?wsSecret=be6d6d51a6cfa2e18f5556d84203e106&wsTime=5b7ceb61",
                "http://ws4.stream.huya.com/hqlive/95431869-2562758913-11006965718867509248-34850612-10057-A-0-1.flv?wsSecret=2c12563c11e827e30189e9d5345bfcb5&wsTime=5b7cd5c4&ratio=2000&u=1523771614&t=100&sv=1808220954",
                "http://ws4.stream.huya.com/hqlive/95431869-2562758913-11006965718867509248-34850612-10057-A-0-1.flv?wsSecret=2c12563c11e827e30189e9d5345bfcb5&wsTime=5b7cd5c4&ratio=1200&u=1523771614&t=100&sv=1808220954",
                "http://ws4.stream.huya.com/hqlive/95431869-2562758913-11006965718867509248-34850612-10057-A-0-1.flv?wsSecret=2c12563c11e827e30189e9d5345bfcb5&wsTime=5b7cd5c4&ratio=500&u=1523771614&t=100&sv=1808220954"};
        String[] names = new String[]{"蓝光", "超清", "高清", "流畅"};

        videoView.findViewById(R.id.exo_refersh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        load(json);
                    }
                }.start();
            }
        });

        videoView.findViewById(R.id.exo_full).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (instance.isPortrait()) {
                    instance.toggleScreen();
                } else {
                    instance.toggleScreen();
                }

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
            }
        });

        videoView.findViewById(R.id.exo_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LiveActivity.this);
                CharSequence[] items = new CharSequence[]{"蓝光", "超清", "高清", "流畅"};
                builder.setSingleChoiceItems(items, streamLine, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                String url = Huya.getStreamLive("http://www.huya.com/" + roomId);
                                switch (which) {
                                    case 0:
                                        break;
                                    case 1:
                                        url += "&ratio=2000&u=1523771614&t=100&sv=1808220954";
                                        break;
                                    case 2:
                                        url += "&ratio=1200&u=1523771614&t=100&sv=1808220954";
                                        break;
                                    case 3:
                                        url += "&ratio=500&u=1523771614&t=100&sv=1808220954";
                                        break;
                                    default:
                                        break;
                                }
                                streamLine = which;
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
                        }.start();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
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
    protected void onStop() {
        super.onStop();
        instance.stop();
    }

    private void load(JSONObject json) {
        try {
            roomId = json.getString("profileRoom");
            String url = Huya.getStreamLive("http://www.huya.com/" + roomId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
                    player.prepare(videoSource);
                    player.setPlayWhenReady(true);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (instance.isPortrait()) {
            // 切换成竖屏
            WindowUtil.cancelFullScreen(this);
            videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            WindowUtil.setFullScreen(this);
            videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }

    }
}
