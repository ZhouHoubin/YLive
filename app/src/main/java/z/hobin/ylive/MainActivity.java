package z.hobin.ylive;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.OnBoomListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import z.hobin.ylive.douyu.DouYu;
import z.hobin.ylive.util.FileUtil;
import z.hobin.ylive.util.HttpUtils;

public class MainActivity extends BaseActivity {

    private AlertDialog loadDialog;
    private TabLayout tableLayout;
    private ViewPager viewPager;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableLayout = findViewById(R.id.tab);
        tableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewPager = findViewById(R.id.viewpager);
        tableLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("position = [" + position + "]");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        loadData(cache.getAsInteger("menu_index", 0));

        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb.setButtonEnum(ButtonEnum.TextInsideCircle);

        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.huya).normalText("虎牙").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.douyu).normalText("斗鱼").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.panda).normalText("熊猫").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.longzhu).normalText("龙珠").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.quanmin).normalText("全民").isRound(false));
        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
//        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
//        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
//        bmb.addBuilder(new TextInsideCircleButton.Builder().normalImageRes(R.drawable.yy).normalText("YY").isRound(false));
        bmb.setOnBoomListener(new OnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                cache.put("menu_index", String.valueOf(index));
                loadData(index);
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

    private void loadData(int index) {
        AlertDialog.Builder loadBuilder = new AlertDialog.Builder(MainActivity.this);
        loadBuilder.setTitle("");
        loadBuilder.setMessage("加载中.........");
        loadDialog = loadBuilder.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case 0://虎牙
                        loadHuya();
                        break;
                    case 1://斗鱼
                        loadDouyu();
                        break;
                    case 2://熊猫
                        loadPanda();
                        break;
                    case 3://龙珠
                        break;
                    case 4://全民tv
                        loadQuanMin();
                        break;
                }
            }
        }, 1000);
    }

    private void loadQuanMin() {
        tableLayout.removeAllTabs();
        clearFragments();
        new Thread() {
            @Override
            public void run() {
                super.run();
                String rawData = HttpUtils.sendGet("https://www.quanmin.tv/json/categories/list.json", null);
                try {
                    JSONArray jsonArray = new JSONArray(rawData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<TabFragment> fragments = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = jsonArray.getJSONObject(i);
                                    Category category = new Category();
                                    category.id1 = jsonObject.getInt("id");
                                    category.name = jsonObject.getString("name");
                                    category.shortName = jsonObject.getString("slug");
                                    category.pic = jsonObject.getString("image");
                                    category.icon = jsonObject.getString("thumb");
                                    category.smallIcon = jsonObject.getString("thumb");
                                    category.data = jsonObject;
                                    fragments.add(Fragments.newInstance(category, Live.Tag.QUANMIN));
                                    tableLayout.addTab(tableLayout.newTab().setText(category.name));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            //viewPager.setId((int) System.currentTimeMillis());
                            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments));
                            viewPager.setOffscreenPageLimit(fragments.size());
                            viewPager.invalidate();
                            if (loadDialog != null && loadDialog.isShowing()) {
                                loadDialog.dismiss();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void loadDouyu() {
        tableLayout.removeAllTabs();
        clearFragments();
        new Thread() {
            @Override
            public void run() {
                super.run();
                String rawData = HttpUtils.sendGet("https://m.douyu.com/api/cate/list?type=", null);
                try {
                    JSONObject rootObject = new JSONObject(rawData);
                    JSONArray jsonArray = rootObject.getJSONObject("data").getJSONArray("cate2Info");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<TabFragment> fragments = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = jsonArray.getJSONObject(i);
                                    Category category = new Category();
                                    category.id1 = jsonObject.getInt("cate1Id");
                                    category.id2 = jsonObject.getInt("cate2Id");
                                    category.name = jsonObject.getString("cate2Name");
                                    category.shortName = jsonObject.getString("shortName");
                                    category.pic = jsonObject.getString("pic");
                                    category.icon = jsonObject.getString("icon");
                                    category.smallIcon = jsonObject.getString("smallIcon");
                                    category.count = jsonObject.getInt("count");
                                    category.data = jsonObject;
                                    fragments.add(Fragments.newInstance(category, Live.Tag.DOUYU));
                                    tableLayout.addTab(tableLayout.newTab().setText(category.name));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            //viewPager.setId((int) System.currentTimeMillis());
                            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments));
                            viewPager.setOffscreenPageLimit(fragments.size());
                            viewPager.invalidate();
                            if (loadDialog != null && loadDialog.isShowing()) {
                                loadDialog.dismiss();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void loadPanda() {
        tableLayout.removeAllTabs();
        clearFragments();
        new Thread() {
            @Override
            public void run() {
                super.run();
                String rawData = HttpUtils.sendGet("http://api.m.panda.tv/index.php?method=category.alllist", null);
                try {
                    JSONObject rootObject = new JSONObject(rawData);
                    JSONArray dataArray = rootObject.getJSONArray("data");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<TabFragment> fragments = new ArrayList<>();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = dataArray.getJSONObject(i);
                                    Category category = new Category();
                                    category.name = jsonObject.getString("cname");
                                    category.shortName = jsonObject.getString("ename");
                                    category.icon = jsonObject.getString("img");
                                    category.count = jsonObject.getInt("ext");
                                    category.data = jsonObject;
                                    fragments.add(Fragments.newInstance(category, Live.Tag.PANDA));

                                    TextView textView = new TextView(getApplicationContext());
                                    textView.setTextColor(Color.RED);
                                    textView.setText(category.name);
                                    tableLayout.addTab(tableLayout.newTab().setCustomView(textView));

                                    JSONArray childArray = jsonObject.getJSONArray("child_data");
                                    for (int j = 0; j < childArray.length(); j++) {
                                        JSONObject childObject = childArray.getJSONObject(j);
                                        Category childCategory = new Category();
                                        childCategory.name = childObject.getString("cname");
                                        childCategory.shortName = childObject.getString("ename");
                                        childCategory.icon = childObject.getString("img");
                                        childCategory.count = childObject.getInt("ext");
                                        childCategory.data = childObject;
                                        fragments.add(Fragments.newInstance(childCategory, Live.Tag.PANDA));
                                        tableLayout.addTab(tableLayout.newTab().setText(childCategory.name));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments));
                            viewPager.setOffscreenPageLimit(fragments.size());
                            viewPager.invalidate();
                            if (loadDialog != null && loadDialog.isShowing()) {
                                loadDialog.dismiss();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void clearFragments() {
        TabAdapter adapter = (TabAdapter) viewPager.getAdapter();
        if (adapter != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            for (int i = 0; i < adapter.getCount(); i++) {//通过遍历清除所有缓存
                final long itemId = adapter.getItemId(i);
                //得到缓存fragment的名字
                String name = adapter.makeFragmentName(viewPager.getId(), itemId);
                //通过fragment名字找到该对象
                TabFragment fragment = (TabFragment) getSupportFragmentManager().findFragmentByTag(name);
                if (fragment != null) {
                    //移除之前的fragment
                    ft.remove(fragment);
                }
            }
            ft.commitNow();
        }
    }

    private void loadHuya() {
        try {
            tableLayout.removeAllTabs();
            clearFragments();

            String rawData = FileUtil.readRaw(getResources(), R.raw.huya);
            JSONArray jsonArray = new JSONArray(rawData);

            List<TabFragment> fragments = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = jsonArray.getJSONObject(i);

                    Category category = new Category();
                    category.id1 = jsonObject.getInt("gid");
                    category.url = jsonObject.getString("href");
                    category.name = jsonObject.getString("title");
                    category.data = jsonObject;

                    fragments.add(Fragments.newInstance(category, Live.Tag.HUYA));

                    tableLayout.addTab(tableLayout.newTab().setText(category.name));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //viewPager.setId((int) System.currentTimeMillis());
            viewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), fragments));
            viewPager.setOffscreenPageLimit(fragments.size());
            viewPager.invalidate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
    }

    private class TabAdapter extends FragmentPagerAdapter {
        private List<TabFragment> fragmentList;

        public TabAdapter(FragmentManager fm, List<TabFragment> fragments) {
            super(fm);
            this.fragmentList = fragments;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        public String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
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

        //解决数据不刷新的问题
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
