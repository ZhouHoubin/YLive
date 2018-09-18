package z.hobin.ylive.huya;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ConsoleMessage;
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
 * 虎牙播放地址
 */
public class HuYaLiveActivity extends LiveActivity implements View.OnClickListener {
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
    private Huya huya;
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
            liveTitle.setText(json.getString("introduction"));
            TextView liveUser = liveInfo.findViewById(R.id.live_user);
            liveUser.setText(json.getString("nick"));
            ImageView liveAvatar = liveInfo.findViewById(R.id.live_avatar);
            Picasso.get().load(json.getString("avatar180")).transform(new CircleImageTransformation()).into(liveAvatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            roomId = json.getString("profileRoom");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        roomUrl = "http://www.huya.com/" + roomId;
        h5RoomUrl = "http://m.huya.com/" + roomId;
        huya = new Huya();
        liveWeb.loadUrl(roomUrl);
        liveWeb.setWebViewClient(new WebViewClient());

        liveMobileWeb.loadUrl(h5RoomUrl);
        liveMobileWeb.setWebContentsDebuggingEnabled(true);
        liveMobileWeb.setWebViewClient(new WebViewClient());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.exo_line:
                if (multiLineInfo == null || multiLineInfo.size() == 0) {
                    return;
                }
                AlertDialog.Builder lineBuilder = new AlertDialog.Builder(HuYaLiveActivity.this);
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
                if (multiRateInfo == null || multiRateInfo.size() == 0) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(HuYaLiveActivity.this);
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
        if (multiLineInfo == null || multiLineInfo.size() == 0) {
            return;
        }
        String url = multiLineInfo.get(line).url;
        if (rate != 0) {
            if (multiRateInfo.size() == 1) {
                rate = 0;
            }
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


    protected class WebViewClient extends android.webkit.WebViewClient {
        private boolean isInsert;

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("m.huya.com")) {
                if (!isInsert) {
                    view.evaluateJavascript("    var script = document.createElement('script');\n" +
                            "    script.setAttribute(\"type\",\"text/javascript\");\n" +
                            "    script.innerHTML = 'document.getElementsByClassName(\\'tanmu_scroll\\')[0].addEventListener(\"DOMNodeInserted\",function(e){\\n' +\n" +
                            "        '        var node = e.target;\\n' +\n" +
                            "        '\\t\\tif(node.className == \\'prop\\'){\\n' +\n" +
                            "        '\\t\\t\\t//礼物\\n' +\n" +
                            "        '\\t\\t\\tconsole.log(\\'[\\'+node.innerText+\\']\\');\\n' +\n" +
                            "        '\\t\\t}else if(node.className == \\'normal\\'){\\n' +\n" +
                            "        '\\t\\t\\t//文字\\n' +\n" +
                            "        '\\t\\t\\tvar nick = node.childNodes[0].innerText;\\n' +\n" +
                            "        '\\t\\t\\tvar text = node.childNodes[1];\\n' +\n" +
                            "        '\\t\\t\\tconsole.log(\\'[\\'+nick+\\':\\'+text.data+\\']\\');\\n' +\n" +
                            "        '\\t\\t}\\n' +\n" +
                            "        '       \\n' +\n" +
                            "        '    });';\n" +
                            "    document.getElementsByTagName(\"head\")[0].appendChild(script);", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                    isInsert = true;
                }

                view.evaluateJavascript("$('#chatArea').css('width','100%');$('#chatArea').css('height','100%');$('#chatArea').css('top','0');$('#chatArea').css('position','fixed');$('.huya-header').css('display','none');$('.live-wrap').css('display','none');$('.live-info-btn').css('display','none');$('#m-container').attr('style','padding-top:0;');$('#m-container > div.live_tab').css('height','0');$('#m-container > div.live_tab').css('display','none');", new ValueCallback<String>() {
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
                        huya.load(value);
                        multiRateInfo = huya.getMultiRateInfo();
                        multiLineInfo = huya.getMultiLineInfo();
                        huyaLiveInfo = huya.getLiveInfo();
                        play(0, 1);
                    }
                });
            }
        }
    }
}
