package z.hobin.ylive;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import z.hobin.ylive.util.HttpUtils;

public class Huya {

    public static String getStreamLive(String url) {
        String html = HttpUtils.sendGet(url, null);
        Document document = Jsoup.parse(html);
        Elements elements = document.select("script");
        String hex = "0000009E10032C3C4C56066C6976657569660D6765744C6976696E67496E666F7D0000750800010604745265711D0000680A0A0300000000000000001620FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF2600361777656226323031377633322E313131302E33266875796146005C0B1300000000%s2300000000%s3300000000000000000B8C980CA80C";

        String channel = "";
        String sid = "";
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            String text = element.html();
            if (text.contains("TT_ROOM_DATA ")) {
                channel = match("\"channel\":\"*(\\d+)\"*", text, 0);
                sid = match("\"sid\":\"*(\\d+)\"*", text, 0);
                channel = channel.replaceAll("\"", "");
                sid = sid.replaceAll("\"", "");

                channel = channel.split(":")[1];
                sid = sid.split(":")[1];

                System.out.println(channel);
                System.out.println(sid);

                System.out.println(new String(HttpUtils.hexStr2BinArr(hex)));

                hex = String.format(Locale.CHINA, hex, zfill(channel), zfill(sid));
            }
        }

        if (channel.equals("0")) {
            System.out.println("live video is offline 1");
            return "";
        }

        html = HttpUtils.sendPost("http://cdn.wup.huya.com/", hex);
        try {
            html = new String(html.getBytes("gbk"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(html);

        if (!html.contains(channel)) {
            System.out.println("live video is offline 2");
            //return;
        }
        String vid = match(String.format(Locale.CHINA, "(%s-%s[^f]+)", channel, sid), html, 0);
        String wsSecret = match("wsSecret=([0-9a-z]{32})", html, 1);
        String wsTime = match("wsTime=([0-9a-z]{8})", html, 1);
        String line = match("://(.+\\.(flv|stream)\\.huya\\.com/(hqlive|huyalive))", html, 0);
        if (TextUtils.isEmpty(line)) {
            System.out.println("live video is offline 3");
            return "";
        }
        //line = line.substring(0, 29);
        return String.format(Locale.CHINA, "http%s/%s.flv?wsSecret=%s&wsTime=%s", line, vid, wsSecret, wsTime);
    }

    private static String zfill(String text) {
        String hex = Integer.toHexString((int) Long.parseLong(text));
        String str = "";
        for (int i = 0; i < 8 - hex.length(); i++) {
            str += "0";
        }
        return str + hex;
    }

    private static String match(String regex, String text, int count) {
        String result = "";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            result = matcher.group(count);
        }
        return result;
    }
}
