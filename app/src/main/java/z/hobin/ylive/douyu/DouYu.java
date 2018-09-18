package z.hobin.ylive.douyu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import z.hobin.ylive.BaseExtrator;
import z.hobin.ylive.HuyaLiveInfo;
import z.hobin.ylive.LineInfo;
import z.hobin.ylive.RateInfo;
import z.hobin.ylive.util.FileUtil;
import z.hobin.ylive.util.HttpUtils;

public class DouYu implements BaseExtrator {
    private String APPKEY = "zNzMV1y4EMxOHS6I5WKm";
    private JSONObject data;
    private String roomId;

    public DouYu(String roomId) {
        this.roomId = roomId;
    }

    private static String match(String regex, String text, int count) {
        String result = "";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            result = matcher.group(count);
        }
        return result;
    }

    @Override
    public void load(String value) {
        try {
            String authstr = String.format(Locale.CHINA, "room/%s?aid=wp&client_sys=wp&time=%s", roomId, String.valueOf(System.currentTimeMillis()).substring(0, 10));

            String authmd5 = FileUtil.md5((authstr + APPKEY));

            String api_url = String.format(Locale.CHINA, "http://www.douyutv.com/api/v1/%s&auth=%s", authstr, authmd5);

            String html = HttpUtils.sendGet(api_url, null);

            data = new JSONObject(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<RateInfo> getMultiRateInfo() {
        return null;
    }

    @Override
    public List<LineInfo> getMultiLineInfo() {
        List<LineInfo> streamInfoList = new ArrayList<>();
        try {
            JSONObject data2 = data.getJSONObject("data");
            String rtmpUrl = data2.getString("rtmp_url");

            LineInfo lineInfo1 = new LineInfo();
            lineInfo1.title = "线路1";
            lineInfo1.url = rtmpUrl + "/" + data2.getString("rtmp_live");
            streamInfoList.add(lineInfo1);

            //LineInfo lineInfo2 = new LineInfo();
            //lineInfo2.title = "线路2";
            //lineInfo2.url = data2.getString("hls_url");

            LineInfo lineInfo2 = new LineInfo();
            lineInfo2.title = "线路2";
            lineInfo2.url = rtmpUrl + "/" + data2.getJSONObject("rtmp_multi_bitrate").getString("middle2");
            streamInfoList.add(lineInfo2);

            LineInfo lineInfo3 = new LineInfo();
            lineInfo3.title = "线路3";
            lineInfo3.url = rtmpUrl + "/" + data2.getJSONObject("rtmp_multi_bitrate").getString("middle");
            streamInfoList.add(lineInfo3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return streamInfoList;
    }

    @Override
    public HuyaLiveInfo getLiveInfo() {
        return null;
    }
}
