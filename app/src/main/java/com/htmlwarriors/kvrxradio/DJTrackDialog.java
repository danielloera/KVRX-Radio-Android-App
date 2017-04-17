package com.htmlwarriors.kvrxradio;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

/**
 * Created by Daniel on 3/25/2016.
 * <p/>
 * :)
 */
public class DJTrackDialog extends DialogFragment {

    private ParcelableTrack track;
    private int color;
    private Context context;
    private RelativeLayout layoutView;
    private TextView text;
    private boolean clickable;
    private Activity attached;

    public DJTrackDialog(){}

    static DJTrackDialog  newInstance(SerializableTrack serializableTrack) {
        DJTrackDialog  f = new DJTrackDialog ();

        Bundle args = new Bundle();
        ParcelableTrack parcTrack = new ParcelableTrack(serializableTrack);
        args.putParcelable("ParcelableTrack", parcTrack);
        f.setArguments(args);
        return f;
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = activity;
        attached = activity;
    }

  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        track = getArguments().getParcelable("ParcelableTrack");
    }

    public Dialog onCreateDialog(Bundle s){
        Dialog d = super.onCreateDialog(s);

        d.getWindow().requestFeature(DialogFragment.STYLE_NO_TITLE);
        return  d;
    }

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
       
        layoutView = (RelativeLayout)inflater.inflate(R.layout.dj_track_dialog_layout, container);

        text = (TextView)layoutView.findViewById(R.id.TrackInfo);
        ImageView albumArt = (ImageView)layoutView.findViewById(R.id.TrackAlbumArt);

        text.setText("Artist: " + track.getArtistName() + "\n" +
                "Album: " + track.getAlbumName() + "\n" +
                "Track: " + track.getTrackName());

        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callArtistActivity();
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    callArtistActivity();
            }
        });

        new AlbumArtGrabber(albumArt).execute();

        return layoutView;
    }

    private void callArtistActivity(){
        if (clickable) {
            Intent i = new Intent(context, Artist_page.class);

            i.putExtra("artist", track.getArtistName());
            i.putExtra("color", color);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.startActivity(i, makeSceneTransitionAnimation(attached).toBundle());
            }else {
                context.startActivity(i);
            }

        }
    }

    private class AlbumArtGrabber extends AsyncTask<Void,Void,Void>{
            ImageView art;
            Bitmap bitmapArt;
        public AlbumArtGrabber(ImageView art){
            this.art = art;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Album currentAlbum = Album.getInfo(track.getArtistName(), track.getAlbumName(),
                    Artist_page.KEY);
            if (currentAlbum != null){
                String url = currentAlbum
                            .getImageURL(ImageSize.MEGA).replace("/174s", "");
                Log.i("DJ PAGE", url);
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.connect();
                    bitmapArt = BitmapFactory.decodeStream(con.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        public void onPostExecute(Void d){
            if(bitmapArt != null) {
                art.setImageBitmap(bitmapArt);
                color = AppUtils.averageColor(bitmapArt);
            }
            ProgressBar bar = (ProgressBar)layoutView.findViewById(R.id.trackProgress);
            bar.setVisibility(View.INVISIBLE);
            clickable = true;
            text.setClickable(clickable);

        }
    }
}
