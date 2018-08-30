package z.hobin.ylive;

import android.os.Bundle;

import org.json.JSONObject;

import z.hobin.ylive.douyu.DouYuTabFragment;
import z.hobin.ylive.huya.HuYaTabFragment;
import z.hobin.ylive.panda.PandaTabFragment;

public class Fragments {
    public static TabFragment newInstance(JSONObject json) {
        Bundle args = new Bundle();
        args.putString("json", json.toString());
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TabFragment newInstance(Category category, Live.Tag tag) {
        Bundle args = new Bundle();
        args.putParcelable("category", category);
        TabFragment fragment = new TabFragment();
        switch (tag) {
            case HUYA:
                fragment = new HuYaTabFragment();
                break;
            case DOUYU:
                fragment = new DouYuTabFragment();
                break;
            case PANDA:
                fragment = new PandaTabFragment();
                break;
        }
        fragment.setArguments(args);
        return fragment;
    }
}
