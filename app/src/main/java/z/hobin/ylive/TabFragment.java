package z.hobin.ylive;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import z.hobin.ylive.huya.HuYaLiveActivity;

public class TabFragment extends Fragment implements OnItemClickListener {
    protected RecyclerView recyclerView;
    protected boolean init;
    protected Category category;
    protected Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCategory();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getCategory();

        recyclerView = view.findViewById(R.id.main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public String getTitle() {
        try {
            getCategory();
            return category.name;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void getCategory() {
        if (category == null) {
            if (getArguments() != null) {
                category = getArguments().getParcelable("category");
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!init && isVisibleToUser) {
            if (category == null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setUserVisibleHint(true);
                    }
                }, 1000);
            } else {
                loadData(1);
                init = true;
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    protected void loadData(int page) {

    }

    @Override
    public void onItemClick(View view, int position) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof JSONObject) {
            JSONObject json = (JSONObject) tag;
            Intent intent = new Intent(getActivity(), HuYaLiveActivity.class);
            intent.putExtra("data", json.toString());
            startActivity(intent);
        }
    }
}
