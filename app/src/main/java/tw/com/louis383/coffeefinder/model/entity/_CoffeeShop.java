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
    protected float wifi;
    @SerializedName("seat")
    @Expose
    protected float seat;
    @SerializedName("quiet")
    @Expose
    protected float quiet;
    @SerializedName("tasty")
    @Expose
    protected float tasty;
    @SerializedName("cheap")
    @Expose
    protected float cheap;
    @SerializedName("music")
    @Expose
    protected float music;
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
    @SerializedName("distance")
    @Expose
    protected double distance;

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

    public float getWifi() {
        return wifi;
    }

    public void setWifi(float wifi) {
        this.wifi = wifi;
    }

    public float getSeat() {
        return seat;
    }

    public void setSeat(float seat) {
        this.seat = seat;
    }

    public float getQuiet() {
        return quiet;
    }

    public void setQuiet(float quiet) {
        this.quiet = quiet;
    }

    public float getTasty() {
        return tasty;
    }

    public void setTasty(float tasty) {
        this.tasty = tasty;
    }

    public float getCheap() {
        return cheap;
    }

    public void setCheap(float cheap) {
        this.cheap = cheap;
    }

    public float getMusic() {
        return music;
    }

    public void setMusic(float music) {
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
