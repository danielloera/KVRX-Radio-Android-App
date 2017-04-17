package com.htmlwarriors.kvrxradio;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jorge on 3/28/2016.
 * ParcelableTrack is meant to be used by the DJTrackDialog fragment
 * in order to display information about the serializableTrack it receives
 * from the DJPage activity.
 */
public class ParcelableTrack extends SerializableTrack implements Parcelable{

    public ParcelableTrack(SerializableTrack serializableTrack){
        super.setAlbumName(serializableTrack.getAlbumName());
        super.setArtistName(serializableTrack.getArtistName());
        super.setTrackName(serializableTrack.getTrackName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(super.getTrackName());
        dest.writeString(super.getAlbumName());
        dest.writeString(super.getArtistName());
    }



    protected ParcelableTrack(Parcel in) {
        super.setTrackName(in.readString());
        super.setAlbumName(in.readString());
        super.setArtistName(in.readString());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        @Override
        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

}
