package z.hobin.ylive.quanming;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import z.hobin.ylive.OnItemClickListener;
import z.hobin.ylive.R;
import z.hobin.ylive.TabFragment;

public class QuanMinTabFragment extends TabFragment {
    @Override
    protected void loadData(int page) {
        super.loadData(page);
        page--;
        String url = "";
        if (page == 0) {
            url = String.format(Locale.CHINA, "https://www.quanmin.tv/json/categories/%s/list.json", category.shortName);
        } else {
            url = String.format(Locale.CHINA, "https://www.quanmin.tv/json/categories/%s/list_%d.json", category.shortName, page);
        }
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
                    JSONArray dataArray = json.getJSONArray("data");
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecAdapter adapter = new RecAdapter(dataArray);
                            adapter.setOnItemClickListener(QuanMinTabFragment.this);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
        public RecAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = View.inflate(parent.getContext(), R.layout.item_main, null);
            return new RecAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecAdapter.ViewHolder holder, int position) {
            String screenshot = null;
            JSONObject itemData = null;
            try {
                itemData = data.getJSONObject(position);
                if(itemData.has("live_thumb")){
                    screenshot = itemData.getString("live_thumb");
                }else if(itemData.has("cover")){
                    screenshot = itemData.getString("cover");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String introduction = "";
            try {
                if (itemData.getInt("status") == 0) {
                    introduction = "[回放]";
                }
                introduction += itemData.getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String nick = null;
            try {
                nick = itemData.getString("nick");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String count = null;
            try {
                long totalCount = itemData.getInt("view");
                if (totalCount < 10000) {
                    count = String.valueOf(totalCount);
                } else {
                    count = String.valueOf(totalCount / 10000) + "万";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Picasso.get().load(screenshot).into(holder.card_screen);
            holder.card_title.setText(introduction);
            holder.card_user.setText(nick);
            holder.card_count.setText(count);

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
            public TextView card_count;


            public ViewHolder(View itemView) {
                super(itemView);
                card_screen = itemView.findViewById(R.id.card_screen);
                card_title = itemView.findViewById(R.id.card_title);
                card_user = itemView.findViewById(R.id.card_user);
                card_count = itemView.findViewById(R.id.card_count);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof JSONObject) {
            JSONObject json = (JSONObject) tag;
            Intent intent = new Intent(getActivity(), QuanMinLiveActivity.class);
            intent.putExtra("data", json.toString());
            startActivity(intent);
        }
    }
}
