package tw.com.louis383.coffeefinder.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by louis383 on 2017/1/16.
 */

public class _CoffeeShop {
    @SerializedName("id")
    @Expose
    protected String id;
    @SerializedName("name")
    @Expose
    protected String name;
    @SerializedName("city")
    @Expose
    protected String city;
    @SerializedName("wifi")
    @Expose
    protected int wifi;
    @SerializedName("seat")
    @Expose
    protected int seat;
    @SerializedName("quiet")
    @Expose
    protected int quiet;
    @SerializedName("tasty")
    @Expose
    protected int tasty;
    @SerializedName("cheap")
    @Expose
    protected int cheap;
    @SerializedName("music")
    @Expose
    protected int music;
    @SerializedName("url")
    @Expose
    protected String url;
    @SerializedName("address")
    @Expose
    protected String address;
    @SerializedName("latitude")
    @Expose
    protected double latitude;
    @SerializedName("longitude")
    @Expose
    protected double longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getWifi() {
        return wifi;
    }

    public void setWifi(int wifi) {
        this.wifi = wifi;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getQuiet() {
        return quiet;
    }

    public void setQuiet(int quiet) {
        this.quiet = quiet;
    }

    public int getTasty() {
        return tasty;
    }

    public void setTasty(int tasty) {
        this.tasty = tasty;
    }

    public int getCheap() {
        return cheap;
    }

    public void setCheap(int cheap) {
        this.cheap = cheap;
    }

    public int getMusic() {
        return music;
    }

    public void setMusic(int music) {
        this.music = music;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
