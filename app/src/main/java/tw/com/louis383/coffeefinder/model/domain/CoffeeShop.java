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
        dest.writeFloat(this.wifi);
        dest.writeFloat(this.seat);
        dest.writeFloat(this.quiet);
        dest.writeFloat(this.tasty);
        dest.writeFloat(this.cheap);
        dest.writeFloat(this.music);
        dest.writeString(this.url);
        dest.writeString(this.address);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.distance);
    }

    public CoffeeShop() {
    }

    protected CoffeeShop(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.city = in.readString();
        this.wifi = in.readFloat();
        this.seat = in.readFloat();
        this.quiet = in.readFloat();
        this.tasty = in.readFloat();
        this.cheap = in.readFloat();
        this.music = in.readFloat();
        this.url = in.readString();
        this.address = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.distance = in.readDouble();
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
