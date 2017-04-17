package com.htmlwarriors.kvrxradio;


import android.os.Parcel;

import java.io.Serializable;

/**
 * SeriazableTrack
 * Used by Schedule and DJPage activities in order
 * to write and read tracks respectively. This saves
 * bundle space when the DJPage activity is called from Schedule
 * , since a hashmap is no longer needed to be passed through
 * the intent.
 */
public class SerializableTrack implements Serializable{
    private String trackName;
    private String albumName;
    private String artistName;

    public SerializableTrack(String trackName, String albumName, String artistName){
        this.trackName = trackName;
        this.albumName = albumName;
        this.artistName = artistName;
    }

    public SerializableTrack(){
        this("NO TRACK", "NO ALBUM", "NO ARTIIST");
    }


    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String n) {
        this.artistName = n;
    }

    public String toString(){
        return trackName + "\t\t" + artistName + "\t\t" + albumName;
    }

    public long getId(){
        return (long)(trackName.hashCode() + artistName.hashCode() + albumName.hashCode());
    }

}
