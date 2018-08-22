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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TabFragment extends Fragment implements OnItemClickListener {
    private RecyclerView recyclerView;
    private boolean init;
    private JSONObject json;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            json = new JSONObject(getArguments().getString("json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            json = new JSONObject(getArguments().getString("json"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerView = view.findViewById(R.id.main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public String getTitle() {
        try {
            String json = getArguments().getString("json");
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!init && isVisibleToUser) {
            if (json == null) {
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

    private void loadData(int page) {
        String url = String.format(Locale.CHINA, "https://www.huya.com/cache.php?m=LiveList&do=getLiveListByPage&gameId=%d&tagAll=0&page=%d", getGameId(), page);
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url).get();
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                try {
                    JSONObject json = new JSONObject(data);
                    System.out.println(data);
                    if (json.getInt("status") == 200) {
                        JSONArray dataArray = json.getJSONObject("data").getJSONArray("datas");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecAdapter adapter = new RecAdapter(dataArray);
                                adapter.setOnItemClickListener(TabFragment.this);
                                recyclerView.setAdapter(adapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private int getGameId() {
        try {
            return json.getInt("gid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onItemClick(View view, int position) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof JSONObject) {
            JSONObject json = (JSONObject) tag;
            Intent intent = new Intent(getActivity(), LiveActivity.class);
            intent.putExtra("data", json.toString());
            startActivity(intent);
        }
    }


    public class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder> {
        private JSONArray data;
        private OnItemClickListener itemClickListener;

        public RecAdapter(JSONArray data) {
            this.data = data;
        }

        public void setOnItemClickListener(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), R.layout.item_main, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String screenshot = null;
            JSONObject itemData = null;
            try {
                itemData = data.getJSONObject(position);
                screenshot = itemData.getString("screenshot");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String roomName = null;
            try {
                roomName = itemData.getString("roomName");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String nick = null;
            try {
                nick = itemData.getString("nick");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Picasso.get().load(screenshot).into(holder.card_screen);
            holder.card_title.setText(roomName);
            holder.card_user.setText(nick);
            holder.itemView.setTag(itemData);

            if (itemClickListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getAdapterPosition();
                        itemClickListener.onItemClick(holder.itemView, pos);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return data.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView card_screen;
            public TextView card_title;
            public TextView card_user;


            public ViewHolder(View itemView) {
                super(itemView);
                card_screen = itemView.findViewById(R.id.card_screen);
                card_title = itemView.findViewById(R.id.card_title);
                card_user = itemView.findViewById(R.id.card_user);
            }
        }
    }
}
