package z.hobin.ylive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import z.hobin.ylive.util.ACache;

public class BaseActivity extends AppCompatActivity {
    //缓存
    protected ACache cache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cache = ACache.get(this);
    }
}
