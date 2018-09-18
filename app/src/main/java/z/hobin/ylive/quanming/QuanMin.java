package z.hobin.ylive.quanming;

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
import z.hobin.ylive.util.HttpUtils;

public class QuanMin implements BaseExtrator {
    private JSONObject data;
    private String roomId;

    public QuanMin(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public void load(String value) {
        String api_url = String.format(Locale.CHINA, "http://www.quanmin.tv/json/rooms/%s/noinfo6.json", roomId);

        String html = HttpUtils.sendGet(api_url);

        try {
            data = new JSONObject(html);
        } catch (JSONException e) {
            e.printStackTrace();
            load(value);
        }

        System.out.println(api_url);
    }

    @Override
    public List<RateInfo> getMultiRateInfo() {
        List<RateInfo> rateInfoList = new ArrayList<>();
        try {
            JSONObject line = data.getJSONArray("room_lines").getJSONObject(0);
            JSONObject flv = line.getJSONObject("flv");
            JSONArray names = flv.names();
            for (int i = 0; i < names.length(); i++) {
                String name = names.getString(i);
                try {
                    Integer.parseInt(name);
                    JSONObject item = flv.getJSONObject(name);
                    RateInfo rateInfo = new RateInfo();
                    rateInfo.name = item.getString("name");
                    rateInfo.rate = item.getInt("quality");
                    rateInfoList.add(rateInfo);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rateInfoList;
    }

    @Override
    public List<LineInfo> getMultiLineInfo() {
        List<LineInfo> streamInfoList = new ArrayList<>();
        try {
            JSONArray lines = data.getJSONArray("room_lines");
            for (int i = 0; i < lines.length(); i++) {
                JSONObject line = lines.getJSONObject(i);
                LineInfo lineInfo = new LineInfo();
                if (line.getString("name").equalsIgnoreCase("ws")) {
                    lineInfo.title = "主线";
                } else {
                    lineInfo.title = "备用线路";
                }
                lineInfo.url = line.getString("name");
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

    public String getStream(String line, String rate) {
        try {
            JSONArray lines = data.getJSONArray("room_lines");
            for (int i = 0; i < lines.length(); i++) {
                JSONObject l = lines.getJSONObject(i);
                if (l.getString("name").equalsIgnoreCase(line)) {
                    JSONObject flv = l.getJSONObject("flv");
                    if (flv.has(rate)) {
                        return flv.getJSONObject(rate).getString("src");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
