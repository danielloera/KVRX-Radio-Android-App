package com.htmlwarriors.kvrxradio;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Tag;
import io.codetail.animation.ViewAnimationUtils;

public class Artist_page extends AppCompatActivity{
    private String LOGTAG = "Artist Page";

    public static final String KEY = "57ee3318536b23ee81d6b27e36997cde";
    private Context context;
    private ImageView cover;
    private int bgColor;
    private int textColor;
    boolean running;


    @Override
    /*
        Grabs the artist name that is sent from
        PlayerActivity.java in order to generate
        a biography page. Artist_page.java stores
        the artist name and other information in their
        respective variables.
     */
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
            getWindow().setEnterTransition(new Explode());
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_page);
        context = this;
        running = true;
        cover = (ImageView)findViewById(R.id.artist_coverart);
        Intent intent = getIntent();
        String artist = "";

        if (intent != null) {

            artist = intent.getStringExtra("artist");
            int color = intent.getIntExtra("color", 0);

            if (color == 0){
                bgColor = Color.parseColor("#37474F");
                textColor = Color.WHITE;
            }
            else{
                bgColor = color;
                textColor = AppUtils.ContrastColor(bgColor);
            }
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.artistLayout);
            layout.setBackground(new ColorDrawable(bgColor));


        }

        getWindow().setTitle(artist);
        setUpStatusBar();
        setUpSupportBar(artist);
        new ArtistGrabber(artist).execute();

    }
    private void setUpSupportBar(String artistName){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(artistName);
            int supportBarColor = bgColor;
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(supportBarColor));
            Spannable text = new SpannableString(getSupportActionBar().getTitle());
            text.setSpan(new ForegroundColorSpan(textColor), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(text);
        }


    }

    private void setUpStatusBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(AppUtils.darkenColor(bgColor));
    }

    public void onBackPressed(){
        super.onBackPressed();
        running  = false;
    }

    /*
        ArtistGraber uses the lastfm API in order to grab
        the artist's tags and biography information by sending
        the artist name to the API's methods.
     */
    public class ArtistGrabber extends AsyncTask<Void, Void, Void> {
        Artist current;
        String artistName;
        Bitmap coverPhotoURL;
        ArrayList<Tag> tagsList;

        public ArtistGrabber(String artistN) {
            artistName = artistN;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            current = Artist.getInfo(artistName, KEY);


            ImageSize coverPhotoSize = ImageSize.MEGA;


            try {


                tagsList = (ArrayList<Tag>) Artist.getTopTags(artistName, KEY);

                HttpURLConnection con = (HttpURLConnection) new URL(current
                        .getImageURL(coverPhotoSize).replace("60x60", "500x500")).openConnection();
                con.connect();
                coverPhotoURL = BitmapFactory.decodeStream(con.getInputStream());

            } catch (Exception e) {
                e.printStackTrace();
                current = null;
            }


            return null;
        }

        private void showError(){
            new AlertDialog.Builder(context)
                    .setTitle("No artist information found!")
                    .setMessage("Sorry, information for \""+artistName+"\" is not available.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    })
                    .show();
        }

        public void onPostExecute(Void result) {
            //if no bio was found display a dialog box
            if (current == null) {
                showError();
            } else {

                TextView tags = (TextView) findViewById(R.id.tags);
                TextView bio = (TextView) findViewById(R.id.artist_bio);
                bio.setText(current.getWikiText());
                bio.setTextColor(textColor);
                String tagString = "";
                //display only half of the tags
                for (int i = 0; i < tagsList.size() / 2; i++) {
                    tagString += tagsList.get(i).getName() + " ";
                }

                tags.setText(tagString);
                tags.setTextColor(textColor);
                ProgressBar bar = (ProgressBar) findViewById(R.id.artist_progress);
                bar.setVisibility(View.GONE);

                tags = (TextView) findViewById(R.id.genreTags);
                tags.setVisibility(View.VISIBLE);
                tags = (TextView) findViewById(R.id.bio);
                tags.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.artistScrollLayout);
                    layout.setBackground(new ColorDrawable(bgColor));
                }

                ((TextView) findViewById(R.id.bio)).setTextColor(textColor);
                ((TextView) findViewById(R.id.genreTags)).setTextColor(textColor);

                if (coverPhotoURL != null) {
                    setupCoverPhoto(coverPhotoURL);
                }

            }

        }


    }

    public void animateBio(final int finalX, final ScrollView scroll, RelativeLayout layout) {

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0,scroll.getTop());
            }
        });

        layout.setVisibility(View.VISIBLE);


        ObjectAnimator animator= ObjectAnimator.ofInt(scroll, "scrollY", finalX);
        animator.setDuration(700);
        animator.start();

    }

    public void revealView(View v){
        if(running){
        // get the center for the clipping circle
        int cx = (int)(v.getWidth()*.9);
        int cy = (int)(cover.getHeight() * .08f);

        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, v.getWidth() *1.5f);
        anim.setDuration(800);
        anim.start();
        }
    }


    //grabs the artist account photo from lastfm
    public void setupCoverPhoto(Bitmap coverPhotoURL){
        cover.setImageBitmap(coverPhotoURL);
        cover.post(new Runnable() {
            @Override
            public void run() {
                cover.setVisibility(View.VISIBLE);
                revealView(cover);
            }
        });

        //adjust info to correct margins and postion
        final ScrollView scroll = (ScrollView) findViewById(R.id.artistScrollView);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.artistScrollLayout);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();

        final int heightScroll = cover.getHeight();
        params.setMargins(0,heightScroll,0,0);//sets bio to the bottom of the image
        layout.setPadding(0,0,0,heightScroll);//makes sure there is enough scrolling

        animateBio(heightScroll / 2,scroll,layout);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


}
