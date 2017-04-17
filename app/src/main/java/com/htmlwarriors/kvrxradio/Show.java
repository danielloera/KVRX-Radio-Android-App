package com.htmlwarriors.kvrxradio;


import android.os.Parcel;
import android.os.Parcelable;

public class Show implements Parcelable {
    private String showName;
    private String showTime;
    private String day;
    private String showID;

    public Show(){}

    public String getShowName() {
        return showName;
    }

    public String getShowID(){
        return showID;
    }

    public void setShowID(String showID){this.showID = showID;}

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String toString(){
        return showID+"\n"+ day + "\n" + showName + "\n" + showTime;

    }

    protected Show(Parcel in) {
        showName = in.readString();
        showTime = in.readString();
        day = in.readString();
        showID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(showName);
        dest.writeString(showTime);
        dest.writeString(day);
        dest.writeString(showID);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Show> CREATOR = new Parcelable.Creator<Show>() {
        @Override
        public Show createFromParcel(Parcel in) {
            return new Show(in);
        }

        @Override
        public Show[] newArray(int size) {
            return new Show[size];
        }
    };

}
