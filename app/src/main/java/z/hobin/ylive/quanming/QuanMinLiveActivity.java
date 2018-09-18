package z.hobin.ylive.quanming;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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
 * 全民播放
 */
public class QuanMinLiveActivity extends LiveActivity implements View.OnClickListener {
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
    private QuanMin quanMin;
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
            liveTitle.setText(json.getString("title"));
            TextView liveUser = liveInfo.findViewById(R.id.live_user);
            liveUser.setText(json.getString("nick"));
            ImageView liveAvatar = liveInfo.findViewById(R.id.live_avatar);
            Picasso.get().load(json.getString("avatar")).transform(new CircleImageTransformation()).into(liveAvatar);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            roomId = json.getString("no");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        roomUrl = "https://www.quanmin.tv/" + roomId;
        h5RoomUrl = "https://m.quanmin.tv/" + roomId;
        quanMin = new QuanMin(roomId);
        liveWeb.loadUrl(roomUrl);
        liveWeb.setWebViewClient(new WebViewClient());

        liveMobileWeb.loadUrl(h5RoomUrl);
        liveMobileWeb.setWebContentsDebuggingEnabled(true);
        liveMobileWeb.setWebViewClient(new WebViewClient());
        liveMobileWeb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    view.evaluateJavascript("document.getElementsByClassName('player-wrap')[0].style='display:none';\n" +
                            "document.getElementsByClassName('w-header')[0].style='display:none';;\n" +
                            "document.getElementsByClassName('chat-box-wrap')[0].style='position:fixed;background:white;height:100%';\n" +
                            "document.getElementsByTagName('header').style='display:none';\n" +
                            "document.querySelector('body > div > footer').style='display:none';", new ValueCallback<String>() {
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
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                super.run();
                quanMin.load(roomId);
                multiRateInfo = quanMin.getMultiRateInfo();
                multiLineInfo = quanMin.getMultiLineInfo();
                huyaLiveInfo = quanMin.getLiveInfo();
                play(0, 1);
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.exo_line:
                if (multiLineInfo == null || multiLineInfo.size() == 0) {
                    return;
                }
                AlertDialog.Builder lineBuilder = new AlertDialog.Builder(QuanMinLiveActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(QuanMinLiveActivity.this);
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
        String url = quanMin.getStream(multiLineInfo.get(line).url, multiRateInfo.get(rate).rate + "");
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
        private boolean isInsert;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http")) {
                return super.shouldOverrideUrlLoading(view, url);
            } else {
                return true;
            }
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.contains("m.quanmin.tv")) {
                if (!isInsert) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.evaluateJavascript("document.getElementsByClassName('qm-cl_box')[0].addEventListener(\"DOMNodeInserted\",function(e){\n" +
                                    "\t\tvar node = e.target;\n" +
                                    "\t\t//文字\n" +
                                    "\t\tif(node.className.indexOf('qm-cl_chat_normal') !== -1){\n" +
                                    "\t\t\tvar nick = node.getElementsByClassName('qm-cl_nick')[0].innerText;\n" +
                                    "\t\t\tvar text = node.getElementsByClassName('qm-cl_text')[0].innerText;\n" +
                                    "\t\t\tconsole.log('['+nick+text+']');\t\n" +
                                    "\t\t}\n" +
                                    "\t});", null);
                        }
                    }, 2000);
                    isInsert = true;
                }
            }
        }
    }
}
