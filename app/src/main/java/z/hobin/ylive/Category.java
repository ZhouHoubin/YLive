package z.hobin.ylive;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class Category implements Parcelable{
    public int id1;
    public int id2;
    //封面
    public String pic;
    //名字
    public String name;
    //图标
    public String icon;
    //小图标
    public String smallIcon;
    //缩写
    public String shortName;
    //直播个数
    public int count;
    //原始数据
    public JSONObject data;
    //地址
    public String url;

    public Category(){

    }


    protected Category(Parcel in) {
        id1 = in.readInt();
        id2 = in.readInt();
        pic = in.readString();
        name = in.readString();
        icon = in.readString();
        smallIcon = in.readString();
        shortName = in.readString();
        count = in.readInt();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id1);
        dest.writeInt(id2);
        dest.writeString(pic);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(smallIcon);
        dest.writeString(shortName);
        dest.writeInt(count);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
