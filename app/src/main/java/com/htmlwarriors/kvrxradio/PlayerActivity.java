package com.htmlwarriors.kvrxradio;

import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;
import io.codetail.animation.ViewAnimationUtils;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;


public class PlayerActivity extends AppCompatActivity{

    final String TAG = "Player Activity";
    private MediaPlayer player;
    static ImageButton reloadBut,playPause, schedule;
    private WifiManager.WifiLock wifiLock;
    static boolean isPlaying,loading;
    private NotificationCompat.Builder notification;
    private NotificationManager mNotificationManager;
    private KVRXReceiver media;
    static int color;
    static String track,artist,album;
    private boolean showNotification;
    Handler handler;
    Window window;

    //use these to test.
    //REMEMBER TO TURN OFF BEFORE PUSHING :-)
    final static boolean TEST = false;
    final static String TESTARTIST = "XTC";
    final static String TESTTRACK = "Fly On The Wall";


    Context context;
    Resources res;
    private ImageView art;
    private TextView songText;
    private TextView artistAlbumText;
    private RemoteViews notificationView;
    private Bitmap coverPhotoBitmap;
    private RelativeLayout songInformationLayout;
    private Runnable runnable;
    private PendingIntent playPausePend, stopPend, openAppPend;

    /*
        Creates the main player interface
        and starts the broadcast to the
        KVRX radio station
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
            getWindow().setEnterTransition(new Explode());
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        createNotifIntents();
        window = getWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        handler = new Handler();


        res = getResources();
        art = (ImageView) findViewById(R.id.albumArt);
        art.post(new Runnable() {
            @Override
            public void run() {
                revealView(art);
            }
        });
        //setup reciever
        setupButtons();
        media = new KVRXReceiver();
        IntentFilter filter = new IntentFilter("com.htmlwarriors.BroadcastReceiver");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(media, filter);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        songInformationLayout = (RelativeLayout) findViewById(R.id.infoBG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            songInformationLayout.setElevation(10f);
        }
        playPause = (ImageButton) findViewById(R.id.playPause);
        songText = (TextView) findViewById(R.id.songTitle);
        artistAlbumText = (TextView) findViewById(R.id.artistAlbum);
        //PercentRelativeLayout notifView =  (PercentRelativeLayout) LayoutInflater.from(context).inflate(R.layout.notification_layout,null);
        notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        isPlaying = false;

        new GetCurrentInfo().execute();
        setupMediaPlayer();
        createPageSync();
        startPageSync();
    }


    public void revealView(View v){
        // get the center for the clipping circle
        int cx = (v.getLeft() + v.getRight()) / 2;
        int cy = (v.getTop() + v.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, v.getWidth() - cx);
        int dy = Math.max(cy, v.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
        anim.setDuration(800);
        anim.start();
    }

    public void onRestart(){
        super.onRestart();

        reloadBut.clearAnimation();
        playPause.clearAnimation();
        schedule.clearAnimation();
        updateNotification();
    }


    public void onDestroy(){
        super.onDestroy();

        //closes all notifcations and deletes the media player
        mNotificationManager.cancelAll();
        wifiLock.release();
        player.release();
        player = null;
        unregisterReceiver(media);


    }

    /*
         createPageSync is a method that refreshes
         the media player every minute to check if
         any artist info has changed. This is what
         enables the application to grab new data
         from the KVRX website and update everything,
         including the artist bio page.
     */
    public void createPageSync(){


        handler = new Handler();
        //Delay the sync by one minute
        final int DELAY = 1000 * 45;
        //initiate the new sync
        runnable = new Runnable() {

            @Override
            public void run() {
                try{

                    Log.i(TAG, "Restarting page check.");

                    new GetCurrentInfo().execute();
                    //also call the same runnable
                    handler.postDelayed(this, DELAY);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }

            }
        };


    }

    private void startPageSync(){
        runnable.run();
    }

    private void stopPageSync(){
        handler.removeCallbacks(runnable);
    }

    private void resetPlayer(){
        stopPageSync();
        startPageSync();
        isPlaying = false;
        if (player != null) {
            player.release();
            player = null;
        }
        setupMediaPlayer();
    }

    // set up the buttons for the media player
    public void setupButtons(){

        schedule = (FloatingActionButton) findViewById(R.id.schedule);
        playPause = (FloatingActionButton) findViewById(R.id.playPause);
        reloadBut = (FloatingActionButton) findViewById(R.id.reload);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
        reloadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPlayer();
            }
        });


        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make sure that the artist available when the user request bio information
                Intent intent = new Intent(getApplicationContext(), Schedule.class);
                    startActivity(intent);
            }
        });


    }

    public void animateAndPullArtistInfo(View v){

        if (artist != null && !artist.trim().equals("")) {
            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();

            display.getSize(size);

            int height = size.y;

            float finalY = (height - playPause.getY()) + playPause.getHeight();

            TranslateAnimation anim1 = new TranslateAnimation(0, 0, 0, finalY);
            anim1.setFillAfter(true);
            anim1.setDuration(150);
            anim1.setFillEnabled(true);

            TranslateAnimation anim2 = new TranslateAnimation(0, 0, 0, finalY);
            anim2.setFillAfter(true);
            anim2.setDuration(150);
            anim2.setStartOffset(150);
            anim2.setFillEnabled(true);

            TranslateAnimation anim3 = new TranslateAnimation(0, 0, 0, finalY);
            anim3.setFillAfter(true);
            anim3.setDuration(150);
            anim3.setStartOffset(300);
            anim3.setFillEnabled(true);

            anim3.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                   callArtistActivity();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            reloadBut.clearAnimation();

            schedule.startAnimation(anim1);
            playPause.startAnimation(anim2);
            reloadBut.startAnimation(anim3);

        }

    }

    private void callArtistActivity(){
        Intent intent = new Intent(getApplicationContext(), Artist_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("artist", artist);
        intent.putExtra("color", color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, makeSceneTransitionAnimation(this).toBundle());
        }else {
            startActivity(intent);
        }
    }


    public void setupMediaPlayer(){

        showNotification  = true;
        if(!loading)
            setLoading(true);

       final ImageButton but  = (ImageButton)findViewById(R.id.playPause);

        player = new MediaPlayer();
        try {
            player.setDataSource("http://tstv-stream.tsm.utexas.edu:8000/kvrx_livestream");
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                setLoading(false);
                but.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));
                isPlaying = true;
                player.start();
                updateNotification();
            }
        });



        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

        setLoading(true);
        player.prepareAsync();
        player.setWakeMode(this.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

    }

    private void createNotifIntents(){

        //setup action for play button
        Intent playPause = new Intent();
        playPause.setAction("com.htmlwarriors.BroadcastReceiver");
        playPause.putExtra("action", 45);
        playPausePend = PendingIntent.getBroadcast(this,345,playPause,PendingIntent.FLAG_CANCEL_CURRENT);

        //setup action for stop button
        Intent stop = new Intent();
        stop.putExtra("action", 65);
        stop.setAction("com.htmlwarriors.BroadcastReceiver");
        stopPend = PendingIntent.getBroadcast(this,221,stop,PendingIntent.FLAG_CANCEL_CURRENT);


        //setup to open app from notification
        Intent openApp = new Intent(getApplicationContext(), PlayerActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openApp.putExtra("action", 30);
        openApp.setAction("com.htmlwarriors.BroadcastReceiver");
        openAppPend = PendingIntent.getActivity(this, 420, openApp,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void updateNotification() {

        notification = new NotificationCompat.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               notification.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        int playPauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
        String playPauseString = playPauseIcon == R.drawable.ic_play ? "Play" : "Pause";
        notification.addAction(playPauseIcon, playPauseString, playPausePend);
        coverPhotoBitmap = coverPhotoBitmap == null ? BitmapFactory.decodeResource(res, R.drawable.kvrxrecord) : coverPhotoBitmap;
        notification.setLargeIcon(coverPhotoBitmap);
        notification.setSmallIcon(R.drawable.ic_headphones)
                .setContentTitle(track)
        .setContentIntent(openAppPend)
        .addAction(R.drawable.ic_stop, "Stop", stopPend)
                .setContentText(artist + " - " + album).setAutoCancel(true);
        notificationView.setBitmap(R.id.notifAlbumArt,"setNotifAlbumArt",coverPhotoBitmap);

        mNotificationManager.notify(1, notification.build());

    }


    public class KVRXReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {

            int action = intent.getIntExtra("action", -1);

            if(action != -1) {

                //determine what button was pressed and call its appropriate method
                if (action == 45) {
                    playPause();

                } else if (action == 65) {
                    stopMusic();

                } else if (action == 30) {
                    startActivity(intent);
                    updateNotification();
                }

            }else {

                Log.i("Network", "Received broadcast");

                ConnectivityManager connectivityManager = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetInfo != null && activeNetInfo.isConnected();

                if (isConnected){
                    Log.i("Network Handler", "Reconnecting app");
                    resetPlayer();
            }else{
                    // notify user you are not online
                    Log.i("Network Handler", "APP DISCONNECTED");
            }




            }
        }
    }

    public void stopMusic(){

        ImageButton but = (ImageButton)findViewById(R.id.playPause);

        but.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));

        if(player != null){
            player.release();
            player = null;
            loading = false;
            isPlaying = false;
        }

        mNotificationManager.cancelAll();
        reloadBut.clearAnimation();
        showNotification = false;

}

    public void playPause(){

        if(player != null){


            showNotification = true;

            //if it is playing then pause it, otherwise play the song
            if (player.isPlaying()) {
                player.pause();
                isPlaying = false;
                playPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play));

            } else {
                player.start();
                isPlaying = true;
                playPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause));

            }
        }else{

            setupMediaPlayer();

        }

        updateNotification();

    }


    /*
        GetCurrentInfo grabs the information that is currently display on the website.
        This method makes a connection to the website and grabs the current html page.
        After grabbing the html page, this method searches for the album, artist, and
        track infromation.
     */
    public class GetCurrentInfo extends AsyncTask<Void,Void,Void>{

        String tempAlbum = "";
        String oldTrack = "";

        @Override
        protected Void doInBackground(Void... params) {
            if(track != null)
                oldTrack = track;


            try{

                HttpURLConnection con =(HttpURLConnection) new URL("http://www.kvrx.org/locallive").openConnection();

               con.setDoInput(true);
               con.setRequestMethod("POST");
               con.connect();
                BufferedReader reader  = new BufferedReader(new InputStreamReader(con.getInputStream()));

                Thread.sleep(2000);

                String line;

                String found = "";

                ArrayList<String> lines  = new ArrayList<>();
                while((line = reader.readLine())!= null){

                    lines.add(line);

                }

                for(String s:lines){
                    if(s.contains("now-playing")){
                        found  = s;
                    }
                }

                Log.i(TAG,found);

                track = found.substring(found.indexOf("track-name")+12);
                track = track.substring(0,track.indexOf("<")).trim();

                artist = found.substring(found.indexOf("artist-name")+13);
                artist = artist.substring(0, artist.indexOf("<")).trim();

                tempAlbum = found.substring(found.indexOf("album-name")+12);
                tempAlbum = tempAlbum.substring(0, tempAlbum.indexOf("<")).trim();


                if(TEST){
                    artist = TESTARTIST;
                    track = TESTTRACK;
                }

                Track temp = Track.getInfo(artist,track,Artist_page.KEY);

                album = temp.getAlbum();



            }catch(Exception e){

                if(album == null)
                    album = "";

                e.printStackTrace();

            }

            return null;
        }


        //displays the information to the user on the media player.
        public void onPostExecute(Void result){

            if(album == null || album.equals(""))
                album = "Unknown Album";
            if(artist == null || artist.equals(""))
                album = "Unknown Artist";

            if(!track.trim().equals("")) {

                if(!oldTrack.equals(track)){
                songText.setText(track);
                artistAlbumText.setText(artist + " - ");
                if(album  != null && !album.equals("")){
                    artistAlbumText.setText(artistAlbumText.getText() + album);
                    new AlbumArtGrabber(artist, album).execute();
                }
                else{
                    artistAlbumText.setText(artistAlbumText.getText()+ tempAlbum);
                }

                artistAlbumText.setSelected(true);
                if(showNotification)
                    updateNotification();

                revealView(songText);
                revealView(artistAlbumText);
                }

            }else{
                songText.setText("OFFLINE");
                artistAlbumText.setText("Check Schedule for next show");
            }




        }
    }

    /*
        The artist and album name grabbed by the GetCurrentInfo method
        is used by AlbumCoverGrabber to grab the album cover for the
        album. If the album cover is not available then it will display
        a default cover art.
     */
    public class AlbumArtGrabber extends AsyncTask<Void,Void,Void> {
        Album current;
        String artistName, albumName;


        public AlbumArtGrabber(String artist, String album) {
            artistName = artist;
            albumName = album;
        }

        protected Void doInBackground(Void... voids){
            try {
                current = Album.getInfo(artistName, albumName, Artist_page.KEY);

                if(current != null){
                    HttpURLConnection con = (HttpURLConnection) new URL(current
                            .getImageURL(ImageSize.MEGA).replace("/174s", "")).openConnection();
                    con.connect();
                    coverPhotoBitmap = BitmapFactory.decodeStream(con.getInputStream());
                }else{
                    coverPhotoBitmap = null;
                }
            } catch (IOException e) {
                Log.i("Album Art", "failed to retrieve art");
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void Result){
            if(coverPhotoBitmap != null){
                art.setImageBitmap(coverPhotoBitmap);
                color = AppUtils.averageColor(coverPhotoBitmap);

                int darkColor = AppUtils.darkenColor(color);
                int lightColor = AppUtils.lightenColor(color);

                songInformationLayout.setBackground(new ColorDrawable(darkColor));

                int contrastColor = AppUtils.ContrastColor(darkColor);
                songText.setTextColor(contrastColor);
                artistAlbumText.setTextColor(contrastColor);

                art.setBackground(new ColorDrawable(lightColor));
                color = AppUtils.darkenColor(color);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(darkColor);
                }

            }
            else{
                art.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.kvrxrecord));
                art.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.defaultArtBGColor)));
                songInformationLayout.setBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.infoBGColor)));
                songText.setTextColor(Color.WHITE);
                artistAlbumText.setTextColor(Color.WHITE);
            }
            revealView(art);
            updateNotification();
        }



    }
    public void setLoading(boolean load){

        if(load){
            final Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
            anim.setRepeatCount(-1);
            anim.setDuration(2000);
            reloadBut.setAnimation(anim);

            reloadBut.startAnimation(anim);

            loading  = true;
        }
        else{
            loading  = false;
            reloadBut.clearAnimation();
    }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
