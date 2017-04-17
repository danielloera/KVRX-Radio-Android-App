package com.htmlwarriors.kvrxradio;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.LinkedList;


public class DJ {
    private Bitmap coverPhoto;
    private String bio;
    private String DJName;
    private Show show;
    private HashMap<String,LinkedList<SerializableTrack>> playlists;

    public DJ(){}
    public DJ(Show show){
        this.show = show;
        playlists = new HashMap<String, LinkedList<SerializableTrack>>();
    }


    public Bitmap getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(Bitmap coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDJName() {
        return DJName;
    }

    public void setDJName(String DJName) {
        DJName = removeDashes(DJName);
        this.DJName = DJName;
    }

    private String removeDashes(String s){
        StringBuilder sb = new StringBuilder();
        for(char c : s.toCharArray()){
            if(c!='-')
                sb.append(c);
            else
                sb.append(" ");
        }
        return sb.toString();
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public HashMap<String, LinkedList<SerializableTrack>> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(HashMap<String, LinkedList<SerializableTrack>> playlists) {
        this.playlists = playlists;
    }
}
