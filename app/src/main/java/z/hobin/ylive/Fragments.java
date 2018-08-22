package z.hobin.ylive;

import android.os.Bundle;

import org.json.JSONObject;

public class Fragments {
    public static TabFragment newInstance(JSONObject json) {
        Bundle args = new Bundle();
        args.putString("json", json.toString());
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
