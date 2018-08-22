package z.hobin.ylive.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于模拟HTTP请求中GET/POST方式
 *
 * @author landa
 */
public class HttpUtils {
    /**
     * 发送GET请求
     *
     * @param url        目的地址
     * @param parameters 请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendGet(String url, Map<String, String> parameters) {
        String result = "";
        BufferedReader in = null;// 读取响应输入流
        StringBuffer sb = new StringBuffer();// 存储参数
        String params = "";// 编码之后的参数
        try {
            if (parameters != null) {
                // 编码请求参数
                if (parameters.size() == 1) {
                    for (String name : parameters.keySet()) {
                        sb.append(name).append("=").append(
                                java.net.URLEncoder.encode(parameters.get(name),
                                        "UTF-8"));
                    }
                    params = sb.toString();
                } else {
                    for (String name : parameters.keySet()) {
                        sb.append(name).append("=").append(
                                java.net.URLEncoder.encode(parameters.get(name),
                                        "UTF-8")).append("&");
                    }
                    String temp_params = sb.toString();
                    params = temp_params.substring(0, temp_params.length() - 1);
                }
            }

            System.out.println(url);
            // 创建URL对象
            java.net.URL connURL = new java.net.URL(url);
            // 打开URL连接
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 57171));
            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL.openConnection();
            // 设置通用属性
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
            httpConn.setRequestProperty("Cookie", "mid=WroscAALAAGihGAMeBrpUI5CQo7c; ig_or=landscape-primary; csrftoken=YYivfXQ3yrxi3ewM6gH2mqrNAzLDzpqM; shbid=776; ds_user_id=3007442637; sessionid=IGSCa55d1588c4c9772094a52844e3d0a7352f6b9e8557862134065bd0427158cdfa%3Aw63tNZXwnF3sSG06ofgjhcOOSoJOWRhm%3A%7B%22_auth_user_id%22%3A3007442637%2C%22_auth_user_backend%22%3A%22accounts.backends.CaseInsensitiveModelBackend%22%2C%22_auth_user_hash%22%3A%22%22%2C%22_platform%22%3A4%2C%22_token_ver%22%3A2%2C%22_token%22%3A%223007442637%3A0BRksWaSyvgvn3NewIRI6iEwxwtmCcQq%3A420085e7db1a3090ffef63519f0c919465a16a7ce823ab632bdd6b832ab250b5%22%2C%22last_refreshed%22%3A1523792140.982083559%7D; rur=ATN; ig_pr=1; ig_vw=1920; ig_vh=949; urlgen=\"{\\\"time\\\": 1523792082\\054 \\\"47.74.19.103\\\": 45102\\054 \\\"164.52.12.59\\\": 63199\\054 \\\"45.76.96.208\\\": 20473}:1f7gGU:hhgzsure2QNv4Egqlynv3nYZdSs\"");
            // 建立实际的连接
            httpConn.connect();
            // 响应头部获取
            Map<String, List<String>> headers = httpConn.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : headers.keySet()) {
                //System.out.println(key + "\t：\t" + headers.get(key));
            }
            // 定义BufferedReader输入流来读取URL的响应,并设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 发送POST请求
     *
     * @param url        目的地址
     * @param parameters 请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendPost(String url, Map<String, String> parameters) {
        String result = "";// 返回的结果
        BufferedReader in = null;// 读取响应输入流
        PrintWriter out = null;
        StringBuffer sb = new StringBuffer();// 处理请求参数
        String params = "";// 编码之后的参数
        try {
            // 编码请求参数
            if (parameters.size() == 1) {
                for (String name : parameters.keySet()) {
                    sb.append(name).append("=").append(
                            java.net.URLEncoder.encode(parameters.get(name),
                                    "UTF-8"));
                }
                params = sb.toString();
            } else {
                for (String name : parameters.keySet()) {
                    sb.append(name).append("=").append(
                            java.net.URLEncoder.encode(parameters.get(name),
                                    "UTF-8")).append("&");
                }
                String temp_params = sb.toString();
                params = temp_params.substring(0, temp_params.length() - 1);
            }
            // 创建URL对象
            java.net.URL connURL = new java.net.URL(url);
            // 打开URL连接
            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL
                    .openConnection();
            // 设置通用属性
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
            // 设置POST方式
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            // 获取HttpURLConnection对象对应的输出流
            out = new PrintWriter(httpConn.getOutputStream());
            // 发送请求参数
            out.write(params);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应，设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn
                    .getInputStream(), "UTF-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 发送POST请求
     *
     * @param url        目的地址
     * @param parameters 请求参数，Map类型。
     * @return 远程响应结果
     */
    public static String sendPost(String url, String parameters) {
        String result = "";// 返回的结果
        BufferedReader in = null;// 读取响应输入流
        OutputStream out = null;
        StringBuffer sb = new StringBuffer();// 处理请求参数
        try {

            // 创建URL对象
            java.net.URL connURL = new java.net.URL(url);
            // 打开URL连接
            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL
                    .openConnection();
            // 设置通用属性
            // 设置POST方式
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            // 获取HttpURLConnection对象对应的输出流
            out = httpConn.getOutputStream();
            // 发送请求参数
            out.write(hexStr2BinArr(parameters));
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应，设置编码方式
            in = new BufferedReader(new InputStreamReader(httpConn
                    .getInputStream(), "UTF-8"));
            String line;
            // 读取返回的内容
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    //16进制字符串转换为byte[]
    public static byte[] hexStr2BinArr(String hexString) {
        int len = hexString.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return b;
    }


    /**
     * 主函数，测试请求
     *
     * @param args
     */
    public static void main(String[] args) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("name", "sarin");
        String result = sendGet("http://www.baidu.com", parameters);
        System.out.println(result);
    }
}