package z.hobin.ylive;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import z.hobin.ylive.util.FileUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tableLayout = findViewById(R.id.tab);
        tableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        ViewPager viewPager = findViewById(R.id.viewpager);
        try {
            String rawData = FileUtil.readRaw(getResources(), R.raw.huya);
            JSONArray jsonArray = new JSONArray(rawData);

            List<TabFragment> fragments = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = jsonArray.getJSONObject(i);
                    fragments.add(Fragments.newInstance(jsonObject));

                    tableLayout.addTab(tableLayout.newTab().setText(jsonObject.getString("title")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments));
            viewPager.setOffscreenPageLimit(fragments.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tableLayout.setupWithViewPager(viewPager);

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setButtonEnum(ButtonEnum.TextInsideCircle);

        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.huya).normalText("虎牙").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.longzhu).normalText("龙珠").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.panda).normalText("熊猫").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.quanmin).normalText("全民").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {

            }

            @Override
            public void onBackgroundClick() {

            }

            @Override
            public void onBoomWillHide() {

            }

            @Override
            public void onBoomDidHide() {

            }

            @Override
            public void onBoomWillShow() {

            }

            @Override
            public void onBoomDidShow() {

            }
        });
    }

    private class TabAdapter extends FragmentPagerAdapter {
        private List<TabFragment> fragmentList;

        public TabAdapter(FragmentManager fm, List<TabFragment> fragments) {
            super(fm);
            this.fragmentList = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentList.get(position).getTitle();
        }
    }

}
