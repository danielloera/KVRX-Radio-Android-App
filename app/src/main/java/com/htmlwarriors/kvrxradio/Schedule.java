package com.htmlwarriors.kvrxradio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Schedule extends AppCompatActivity{

    private final String TAG = "Schedule Activity";
    private static Map<String, ArrayList<Show>> schedule;
    private static List<Show> shows = new ArrayList<>();
    private static ExpandableListView expListView;
    final private static String[] days = {"Sundays", "Mondays", "Tuesdays",
                                    "Wednesdays", "Thursdays", "Fridays",
                                    "Saturdays"};
    static ProgressDialog dialog;
    Context context;
    static boolean running;
    static ScheduleDownloader downloader;
    static ScheduleListAdapter expListAdapter;

    File showFolder,showFile;
    private LayoutInflater inflater;
    private boolean clicked;
    private File djPic;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
            getWindow().setEnterTransition(new Explode());
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        deleteOldSchedule();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        schedule = new LinkedHashMap<>();
        inflater = getLayoutInflater();

        if (savedInstanceState != null)
            running = savedInstanceState.getBoolean("running", false);

        File folder = context.getFilesDir();
        djPic = new File(folder,"photo.png");

        getWindow().setTitle("Schedule");
        if(getSupportActionBar() != null)
        getSupportActionBar().setTitle("Schedule");

        showFolder = context.getDir("shows", Context.MODE_PRIVATE);
        showFolder.mkdirs();
        showFile = new File(showFolder, "shows.txt");

        if (!showFile.exists()) {
            makeShowFile();
        } else {
            initiateFileWithFile();
        }
    }

    public void onSaveInstanceState(Bundle state){
        state.putBoolean("running", running);
    }

    private void makeShowFile(){
        dialog = new ProgressDialog(this);

        dialog.setTitle("Gathering Schedule");
        dialog.setMessage(
                "Hang tight dudes...\n");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                expListView.post(new Runnable() {
                    @Override
                    public void run() {
                        expListView.expandGroup(day);
                        expListView.setSelectedGroup(day);
                    }
                });
            }
        });
        dialog.show();


        downloader = new ScheduleDownloader();
        downloader.execute();
    }

    private void deleteOldSchedule(){
        File showFolder = context.getDir("shows", Context.MODE_PRIVATE);
        boolean newDirMade = showFolder.mkdir();
        File showFile = new File(showFolder, "shows.txt");
        if(showFile.exists()){
            boolean deleted = showFile.delete();
        }
    }
    public void onResume(){
        super.onResume();
        clicked = false;
    }

    private class ScheduleDownloader extends AsyncTask<Void,Void,Void> {

        public void getScheduleFromWeb(){
            dialog.setMax(100);
            try {
                URL url = new URL("https://danielloera.co/kvrx/shows.txt");
                URLConnection conexion = url.openConnection();
                conexion.connect();
                InputStream is = url.openStream();

                FileOutputStream fos = new FileOutputStream(showFolder + "/shows.txt");
                byte data[] = new byte[1024];
                int count = 0;
                while ((count = is.read(data)) != -1) {
                    fos.write(data, 0, count);
                }
                is.close();
                fos.close();
            } catch (Exception e) {
                Log.e("ERROR DOWNLOADING", "Unable to download" + e.getMessage());
                Snackbar.make(expListView,"Schedule Failed to Download :(",Snackbar.LENGTH_SHORT);
                onBackPressed();
            }
        }
        protected Void doInBackground(Void... params) {

            running = true;
            getScheduleFromWeb();
            return null;
        }
        public void onPostExecute(Void result){
            getShowsFromFile();
            running = false;
            fillShowMap();
            initiateUI();
        }
    }

    public void getShowsFromFile(){

        shows = new ArrayList<>();
        try {

            Scanner s = new Scanner(showFile);
            while(s.hasNextLine()){
                Show temp = new Show();
                String link = s.nextLine();
                temp.setShowID(link.substring(link.indexOf("ms/")+3));
                String name = s.nextLine();
                String date = s.nextLine();
                String day = date.substring(0,date.indexOf(" "));
                String time = date.substring(date.indexOf("at ")+ 3);
                temp.setDay(day);
                temp.setShowTime(time);
                temp.setShowName(name);
                //Log.i(TAG, temp.toString());
                shows.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void fillShowMap(){
        for(Show s : shows){
            String currentDay = s.getDay();
            ArrayList<Show> currentList = schedule.get(currentDay);
            if(currentList == null){
                currentList = new ArrayList<>();
                schedule.put(currentDay,currentList);
            }
            currentList.add(s);
        }
    }

    private class ScheduleListAdapter extends BaseExpandableListAdapter {

        public Object getChild(int groupPosition, int childPosition) {
            return schedule.get(days[groupPosition]).get(childPosition);
        }

        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final Show show = (Show) getChild(groupPosition, childPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.child_item, null);
            }
            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.childTextView);

            txtListChild.setText(show.getShowName() + "\t\t" + show.getShowTime());

            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return schedule.get(days[groupPosition]).size();
        }

        public long getChildId(int group,int child){
            return Long.parseLong(schedule.get(days[group]).get(child).getShowID());
        }

        public Object getGroup(int groupPosition) {
            return days[groupPosition];
        }

        public int getGroupCount() {
            return days.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String dayName = (String) getGroup(groupPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.group_item,
                        null);
            }
            TextView item = (TextView) convertView.findViewById(R.id.groupTextView);
            item.setTypeface(null, Typeface.BOLD);
            item.setText(dayName);
            if(groupPosition == day){
                item.setTextColor(ContextCompat.getColor(context,R.color.infoBGColor));
            }

            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public void initiateFileWithFile(){
        getShowsFromFile();
        fillShowMap();
        initiateUI();
        expListView.post(new Runnable() {
            @Override
            public void run() {

                expListView.expandGroup(day);
                expListView.setSelectedGroup(day);
            }
        });
    }

    public void initiateUI(){

        Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_WEEK) - 1;

        expListAdapter = new ScheduleListAdapter();
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                if(!clicked){
                    clicked = true;
                RelativeLayout childView = (RelativeLayout)v;
                ProgressBar bar = (ProgressBar)childView.findViewById(R.id.scheduleProgress);
                ArrayList<Show> temp = schedule.get(days[groupPosition]);

                 new FetchDJInfo(temp.get(childPosition), bar, parent).execute();

                }

                return false;
            }
        });

        dialog.dismiss();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    private class FetchDJInfo extends AsyncTask<Void,Void,Void> {
        Bitmap coverPhoto;
        DJ dj;
        ProgressBar bar;
        ExpandableListView parent;
        public FetchDJInfo(Show show,ProgressBar bar, ExpandableListView parent){
            this.dj = new DJ(show);
            this.bar = bar;
            this.parent = parent;
        }

        public BufferedReader getPageReader(){
            HttpURLConnection con;
            BufferedReader reader = null;
            try {
                con = (HttpURLConnection) new URL("http://www.kvrx.org/schedule/programs/"
                        + dj.getShow().getShowID()).openConnection();

                con.connect();


                reader = new BufferedReader
                        (new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return reader;
        }

        public Bitmap getCoverPhoto() {
            try {

                BufferedReader reader = getPageReader();
                String url = findDJImageTag("foaf:Image", reader);

                url = getNameOfPhoto(url);
                HttpURLConnection con  = (HttpURLConnection) new URL(url).openConnection();
                con.connect();

                coverPhoto = BitmapFactory.decodeStream(con.getInputStream());

            }catch(IOException e){
                e.printStackTrace();
            }

            return coverPhoto;
        }

        public String getBio(){
            try {

                BufferedReader reader = getPageReader();

                String bio = findDJBioTag("program-description", reader);
                if(bio.contains(">") && bio.contains("</p"))
                    return AppUtils.removeHTMLJunk(bio.substring(bio.indexOf(">")+1, bio.indexOf("</p")));
                else
                    return "No Bio Was Found";

            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        private String findDJImageTag(String contains, BufferedReader reader) throws IOException{
            String line;
            String found = "NOTHING FOUND";
            while((line = reader.readLine())!= null){
                if(line.contains(contains)){
                    found = line;
                    break;
                }
            }
            return found;
        }

        private String findDJBioTag(String contains, BufferedReader reader) throws IOException{
            String line;
            boolean isNext = false;
            String found = "NOTHING FOUND";
            while((line = reader.readLine())!= null){
                if(isNext){
                    found = line;
                    break;
                }
                if(line.contains(contains)){
                    isNext = true;
                }else if(dj.getDJName() == null && line.contains("/staff/")){
                    dj.setDJName(line.substring(line.indexOf("/staff/") + 7, line.indexOf("\"", line.indexOf("/staff/"))).toUpperCase());
                }
            }
            return found;
        }

        private String getNameOfPhoto(String url){
            //ex. 	<a href="/staff/mu"><img typeof="foaf:Image" src="http://www.kvrx.org/sites/default/files/styles/user-image-tiny/public/staffphotos/yama.jpg?itok=BKs2o6xg" width="144" height="144" alt="" /></a>
            if(url.contains(" src=") && url.contains("?"))
                url = url.substring(url.indexOf(" src=") + 6, url.indexOf("?"));
            url = url.replace("-tiny", "");
            Log.i("DJActivity ,IMAGE URL: ", url);


            return url;
        }
        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, "fetching DJ info");

            dj.setCoverPhoto(getCoverPhoto());
            dj.setBio(getBio());
            getPlayList(dj.getPlaylists());

            return null;
        }

        public void getPlayList(HashMap<String,LinkedList<SerializableTrack>> playlist){
            BufferedReader reader = getPageReader();

            String line;
            String date = null;
            LinkedList<SerializableTrack> serializableTracks = new LinkedList<>();
            try {
                while((line = reader.readLine()) != null){

                    if(line.contains("data-date")){
                        if(date != null)
                            playlist.put(date, serializableTracks);
                        date = getDateFromHTML(line);
                    }
                    if(line.contains("playlist-tracks")){
                        serializableTracks = getTracksFromHTML(line);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public LinkedList<SerializableTrack> getTracksFromHTML(String line){
            Scanner s = new Scanner(line);
            s.useDelimiter("playlist");
            LinkedList<SerializableTrack> list = new LinkedList<>();
            while(s.hasNext()) {
                String currentToken = s.next();
                if (currentToken.contains("track-name")) {
                    SerializableTrack t = new SerializableTrack();
                    if (currentToken.contains(">") && currentToken.contains("</"))
                        t.setTrackName(getTrackInfoFromHTML(currentToken));
                    if(!t.getTrackName().equalsIgnoreCase("track name")) {
                        currentToken = s.next();
                        if (currentToken.contains(">") && currentToken.contains("</"))
                            t.setArtistName(getTrackInfoFromHTML(currentToken));
                        currentToken = s.next();
                        if (currentToken.contains(">") && currentToken.contains("</"))
                            t.setAlbumName(getTrackInfoFromHTML(currentToken));
                        list.add(t);
                    }
                } else {
                    s.next();
                }
            }
            return list;
        }

        public String getTrackInfoFromHTML(String currentToken){
            String result  = currentToken.substring(currentToken.indexOf(">")+1, currentToken.indexOf("</"));
            if(result.contains("<a ")) {
                result = result.substring(result.indexOf(">")+1,result.length());
            }
            result = AppUtils.removeHTMLJunk(result);
            return result;
        }

        public String getDateFromHTML(String line){ //ex. <h4 data-date="02/14/2016">February 14, 2016 Show</h4>
            return line.substring(line.indexOf(">")+1,line.indexOf(" Show"));
        }

        public void onPreExecute(){
            //super.onPreExecute();
            bar.setVisibility(View.VISIBLE);
        }


        public void onPostExecute(Void v){

            Intent i = new Intent(context,DJPage.class);

            i.putExtra("bio", dj.getBio());
            i.putExtra("showName", dj.getShow().getShowName());
            i.putExtra("djName", dj.getDJName());
            Bitmap bitmap = dj.getCoverPhoto();


            File playlistFile = new File(getFilesDir(), "playlist");
            writePlayListToFile(dj.getPlaylists(), playlistFile);

            i.putExtra("playlist", playlistFile.getAbsolutePath());

            writeCoverPhotoToFile();

            if(bitmap != null)
                i.putExtra("coverPhoto", djPic.getAbsolutePath());
            else
                i.putExtra("coverPhoto","NOTHING");

            bar.setVisibility(View.INVISIBLE);
            launchDJPage(i);

        }

        private void writePlayListToFile(HashMap<String, LinkedList<SerializableTrack>> playList,
                                         File playlistFile){
            try {
                FileOutputStream writeObject = new FileOutputStream(playlistFile);
                ObjectOutputStream object = new ObjectOutputStream(writeObject);
                object.writeObject(playList);
                object.flush();
                object.close();
                writeObject.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        private void writeCoverPhotoToFile(){
            Bitmap bitmap = dj.getCoverPhoto();
            djPic = new File(getFilesDir(), "photo.png");
            Log.i("exists", djPic.exists() + "");
            try {
                //not necessary
                //boolean create = djPic.createNewFile();
                //Log.i("created", create + "");
                FileOutputStream out = new FileOutputStream(djPic);
                Log.i("path", djPic.getAbsolutePath());
                if(bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void launchDJPage(Intent intent){
            startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
