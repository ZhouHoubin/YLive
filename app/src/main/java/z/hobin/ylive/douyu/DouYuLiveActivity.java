package z.hobin.ylive.douyu;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import z.hobin.ylive.HuyaLiveInfo;
import z.hobin.ylive.LineInfo;
import z.hobin.ylive.LiveActivity;
import z.hobin.ylive.R;
import z.hobin.ylive.RateInfo;
import z.hobin.ylive.util.CircleImageTransformation;

/**
 * 斗鱼播放
 */
public class DouYuLiveActivity extends LiveActivity implements View.OnClickListener {
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
    private DouYu douYu;
    //画质列表
    private List<RateInfo> multiRateInfo = new ArrayList<>();
    //线路列表
    private List<LineInfo> multiLineInfo = new ArrayList<>();
    //直播信息
    private HuyaLiveInfo huyaLiveInfo = null;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String data = getIntent().getStringExtra("data");
        try {
            json = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TextView liveTitle = liveInfo.findViewById(R.id.live_title);
            liveTitle.setText(json.getString("roomName"));
            TextView liveUser = liveInfo.findViewById(R.id.live_user);
            liveUser.setText(json.getString("nickname"));
            ImageView liveAvatar = liveInfo.findViewById(R.id.live_avatar);
            Picasso.get().load(json.getString("avatar")).transform(new CircleImageTransformation()).into(liveAvatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            roomId = json.getString("rid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        roomUrl = "https://www.douyu.com/" + roomId;
        h5RoomUrl = "https://m.douyu.com/" + roomId;
        douYu = new DouYu(roomId);
        liveWeb.loadUrl(roomUrl);
        liveWeb.setWebViewClient(new WebViewClient());

        liveMobileWeb.loadUrl(h5RoomUrl);
        liveMobileWeb.setWebContentsDebuggingEnabled(true);
        liveMobileWeb.setWebViewClient(new WebViewClient());

        new Thread() {
            @Override
            public void run() {
                super.run();
                douYu.load(roomUrl);
                multiRateInfo = douYu.getMultiRateInfo();
                multiLineInfo = douYu.getMultiLineInfo();
                huyaLiveInfo = douYu.getLiveInfo();
                play(0, 1);
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exo_line:
                AlertDialog.Builder lineBuilder = new AlertDialog.Builder(DouYuLiveActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(DouYuLiveActivity.this);
                CharSequence[] items = new CharSequence[multiRateInfo.size()];
                for (int i = 0; i < multiRateInfo.size(); i++) {
                    RateInfo streamInfo = multiRateInfo.get(i);
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
        }
    }

    protected void play(int line, int rate) {
        String url = multiLineInfo.get(line).url;
        if (rate != 0 && multiRateInfo != null) {
            if (multiRateInfo.size() == 1) {
                rate = 0;
            }
        }
        System.out.println("===================\r\n" + url);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
                player.prepare(videoSource);
                player.setPlayWhenReady(true);
            }
        });
    }


    protected class WebViewClient extends android.webkit.WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("m.douyu.com")) {
                view.setVisibility(View.VISIBLE);
//                view.evaluateJavascript("$('#chatArea').css('width','100%');$('#chatArea').css('height','100%');$('#chatArea').css('top','0');$('#chatArea').css('position','fixed');$('.huya-header').css('display','none');$('.live-wrap').css('display','none');$('.live-info-btn').css('display','none');$('#m-container').attr('style','padding-top:0;');$('#m-container > div.live_tab').css('height','0');", new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                view.setVisibility(View.VISIBLE);
//                            }
//                        }, 1000);
//                    }
//                });
            } else {
                //play(0, 1);
            }
        }
    }
}
