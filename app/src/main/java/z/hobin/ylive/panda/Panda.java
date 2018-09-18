package z.hobin.ylive.panda;

import com.google.android.exoplayer2.ExoPlayer;

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

public class Panda implements BaseExtrator {
    private JSONObject data;
    private String roomId;

    public Panda(String roomId) {
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
        String api_url = String.format(Locale.CHINA, "http://www.panda.tv/api_room_v2?roomid=%s&__plat=pc_web&_=%s", roomId, String.valueOf(System.currentTimeMillis()).substring(0, 10));

        String html = HttpUtils.sendGet(api_url, null);

        try {
            JSONObject json = new JSONObject(html);
            data = json.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(api_url);
    }

    @Override
    public List<RateInfo> getMultiRateInfo() {
        try {
            JSONObject stream_addr = data.getJSONObject("videoinfo").getJSONObject("stream_addr");
            int cdnRate = Integer.parseInt(data.getJSONObject("videoinfo").getString("cdn_rate"));
            List<RateInfo> rateInfoList = new ArrayList<>();
            if (stream_addr.has("HD") && stream_addr.getInt("HD") == 1) {
                RateInfo rateInfo = new RateInfo();
                rateInfo.name = "高清";
                rateInfo.rateString = "mid";
                rateInfoList.add(rateInfo);
            }
            if (stream_addr.has("SD") && stream_addr.getInt("SD") == 1) {
                RateInfo rateInfo = new RateInfo();
                rateInfo.name = "超清";
                if (cdnRate >= 3000) {
                    rateInfo.rateString = "3000";
                    rateInfo.name = "超清3M";
                }
                rateInfoList.add(rateInfo);
            }
            if (stream_addr.has("OD") && stream_addr.getInt("OD") == 1) {
                RateInfo rateInfo = new RateInfo();
                rateInfo.name = "OD";
                rateInfoList.add(rateInfo);
            }
            if (stream_addr.has("LD_S") && stream_addr.getInt("LD_S") == 1) {
                RateInfo rateInfo = new RateInfo();
                rateInfo.name = "蓝光1";
                if (cdnRate >= 3800) {
                    rateInfo.rateString = "4000";
                    rateInfo.name = "蓝光4M";
                }
                rateInfoList.add(rateInfo);
            }
            if (stream_addr.has("LD_H") && stream_addr.getInt("LD_H") == 1) {
                RateInfo rateInfo = new RateInfo();
                rateInfo.name = "蓝光2";
                if (cdnRate >= 4800) {
                    rateInfo.name = "蓝光5M";
                }
                rateInfoList.add(rateInfo);
            }
            return rateInfoList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<LineInfo> getMultiLineInfo() {
        List<LineInfo> streamInfoList = new ArrayList<>();
        try {
            String roomKey = data.getJSONObject("videoinfo").getString("room_key");
            String plflag = data.getJSONObject("videoinfo").getString("plflag").split("_")[1];
            String plflag_list = data.getJSONObject("videoinfo").getString("plflag_list");
            JSONObject plflagList = new JSONObject(plflag_list);
            String rid = plflagList.getJSONObject("auth").getString("rid");
            if (rid.contains("-")) {
                //rid = "110557316";
            }
            String sign = plflagList.getJSONObject("auth").getString("sign");
            String time = plflagList.getJSONObject("auth").getString("time");

            if (plflagList.has("main")) {
                LineInfo mainLineInfo = new LineInfo();
                String url = String.format(Locale.CHINA, "http://pl%s.live.panda.tv/live_panda/%s@.flv?sign=%s&ts=%s&rid=%s", plflag, roomKey, sign, time, rid);
                mainLineInfo.title = "主线路";
                mainLineInfo.url = url;
                streamInfoList.add(mainLineInfo);
            }

            JSONArray backUp = plflagList.getJSONArray("backup");
            for (int i = 0; i < backUp.length(); i++) {
                String backUpData = backUp.getString(i);
                plflag = backUpData.split("_")[1];

                LineInfo mainLineInfo = new LineInfo();
                String url = String.format(Locale.CHINA, "http://pl%s.live.panda.tv/live_panda/%s@.flv?sign=%s&ts=%s&rid=%s", plflag, roomKey, sign, time, rid);
                mainLineInfo.title = "备用线路" + (i + 1);
                mainLineInfo.url = url;
                streamInfoList.add(mainLineInfo);
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
