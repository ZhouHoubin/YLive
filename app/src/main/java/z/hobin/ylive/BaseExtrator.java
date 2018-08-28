package z.hobin.ylive;

import java.util.List;

public interface BaseExtrator {
    void load(String value);

    List<RateInfo> getMultiRateInfo();

    List<LineInfo> getMultiLineInfo();

    HuyaLiveInfo getLiveInfo();
}
