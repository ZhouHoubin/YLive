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
        String authstr = String.format(Locale.CHINA, "room/%s?aid=wp&client_sys=wp&time=%s", roomId, String.valueOf(System.currentTimeMillis()).substring(0, 10));

        String authmd5 = FileUtil.md5((authstr + APPKEY));

        String api_url = String.format(Locale.CHINA, "http://www.douyutv.com/api/v1/%s&auth=%s", authstr, authmd5);

        String html = HttpUtils.sendGet(api_url, null);

        try {
            JSONObject json = new JSONObject(html);
            data = json;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(api_url);
    }

    @Override
    public List<RateInfo> getMultiRateInfo() {
        return null;
    }

    private String getRtmp(int rate) {
        String rtmp_url = "";
        try {
            //http://hdl1a.douyucdn.cn/live/606118r8d0VtIzy3_900.flv?wsAuth=c496426466e42e2cc6c86466aa5fc4ad&token=app-wp-0-606118-1d4dd5414ad5da0c8c67f1fe983b3d70&logo=0&expire=0&pt=1
            JSONObject stream = data.getJSONObject("data").getJSONObject("rtmp_multi_bitrate");
            rtmp_url = data.getJSONObject("data").getString("rtmp_url");
            if (rate == 0) {
                rtmp_url += "/" + stream.getString("middle");
            } else if (rate == 1) {
                rtmp_url += "/" + stream.getString("middle2");
            } else {
                rtmp_url += "/" + stream.getString("middle");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rtmp_url;
    }

    @Override
    public List<LineInfo> getMultiLineInfo() {
        List<LineInfo> streamInfoList = new ArrayList<>();
        try {
            JSONArray cdnsWithName = data.getJSONObject("data").getJSONArray("cdnsWithName");
            for (int i = 0; i < cdnsWithName.length(); i++) {
                JSONObject item = cdnsWithName.getJSONObject(i);
                LineInfo lineInfo = new LineInfo();
                lineInfo.data = item;
                lineInfo.title = item.getString("name");
                lineInfo.url = getRtmp(1);
                streamInfoList.add(lineInfo);
            }
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
