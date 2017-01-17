package tw.com.louis383.coffeefinder.model.domain;

import android.os.Parcel;
import android.os.Parcelable;

import tw.com.louis383.coffeefinder.model.entity._CoffeeShop;

/**
 * Created by louis383 on 2017/1/16.
 */

public class CoffeeShop extends _CoffeeShop implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.city);
        dest.writeInt(this.wifi);
        dest.writeInt(this.seat);
        dest.writeInt(this.quiet);
        dest.writeInt(this.tasty);
        dest.writeInt(this.cheap);
        dest.writeInt(this.music);
        dest.writeString(this.url);
        dest.writeString(this.address);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
    }

    public CoffeeShop() {
    }

    protected CoffeeShop(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.city = in.readString();
        this.wifi = in.readInt();
        this.seat = in.readInt();
        this.quiet = in.readInt();
        this.tasty = in.readInt();
        this.cheap = in.readInt();
        this.music = in.readInt();
        this.url = in.readString();
        this.address = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
    }

    public static final Parcelable.Creator<CoffeeShop> CREATOR = new Parcelable.Creator<CoffeeShop>() {
        @Override
        public CoffeeShop createFromParcel(Parcel source) {
            return new CoffeeShop(source);
        }

        @Override
        public CoffeeShop[] newArray(int size) {
            return new CoffeeShop[size];
        }
    };
}
