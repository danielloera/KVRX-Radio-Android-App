package com.htmlwarriors.kvrxradio;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

public class DJPage extends AppCompatActivity {


    final String TAG = "DJ Page";
    private Bitmap coverPhoto;
    private String bio;
    private String showName;
    private String djName;
    private TreeMap<String, LinkedList<SerializableTrack>> playlist;
    private ImageView djImage;
    private TextView djShowName;
    private TextView djBio;
    private LayoutInflater inflater;
    private ExpandableListView playlistView;
    private FragmentManager fragmentManager;
    private File coverPath;
    private File coverPathPlaylist;
    private int coverPhotoColor;
    private int textColor;
    private int lastGroup = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Fade());
            getWindow().setEnterTransition(new Explode());
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_djpage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inflater = getLayoutInflater();

        fragmentManager = getSupportFragmentManager();
        playlistView = (ExpandableListView) findViewById(R.id.playListView);
        djImage = (ImageView)findViewById(R.id.DJImage);
        djShowName = (TextView)findViewById(R.id.DJShowName);
        djBio = (TextView) findViewById(R.id.DJBio);

        Intent intent = getIntent();

        if(intent != null) {
            String coverPhotoPath = intent.getStringExtra("coverPhoto");
            String playListPath = intent.getStringExtra("playlist");
            HashMap<String,ArrayList<SerializableTrack>> hashMap = getPlaylist(playListPath);

            this.coverPhoto = getCoverPhoto(coverPhotoPath);
            this.bio = intent.getStringExtra("bio");
            //old method of geeting playlist
            //HashMap<String,ArrayList<SerializableTrack>> hashMap = (HashMap<String, ArrayList<SerializableTrack>>) intent.getSerializableExtra("playlist");
            this.playlist = AppUtils.toTreeMap(hashMap);
            this.showName = intent.getStringExtra("showName");
            this.djName = intent.getStringExtra("djName");
            djShowName.setText(showName);
        }
    }

    public void onResume(){
        super.onResume();
        if(lastGroup != -1){
            playlistView.expandGroup(lastGroup);
            playlistView.setSelectedGroup(lastGroup);
        }
    }

    private Bitmap getCoverPhoto(String coverPhotoPath){
        Bitmap result = null;
        if(!coverPhotoPath.equals("NOTHING")){
            Log.i("TEST",coverPhotoPath);
            coverPath = new File(coverPhotoPath);
            Log.i("coverpath", coverPath.getAbsolutePath());
            Log.i("coverpath==path", coverPath.getAbsolutePath().equals(coverPhotoPath) + "");
            result = BitmapFactory.decodeFile(coverPath.getAbsolutePath());
        }

        return result;
    }

    private HashMap<String, ArrayList<SerializableTrack>> getPlaylist(String playListPath){
        HashMap<String, ArrayList<SerializableTrack>> result = new HashMap<>();
        coverPathPlaylist = new File(playListPath);
        try {
            FileInputStream readPlaylist = new FileInputStream(coverPathPlaylist);
            ObjectInputStream readObj = new ObjectInputStream(readPlaylist);
            result = (HashMap<String, ArrayList<SerializableTrack>>)readObj.readObject();
            readObj.close();
            readPlaylist.close();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException f){
            f.printStackTrace();
        }
        return result;
    }

    public void onBackPressed(){
        super.onBackPressed();
        if(coverPath != null) {
            Log.i("playlistDeleted", this.coverPathPlaylist.delete() + "");
            Log.i("playlistExists", this.coverPathPlaylist.exists() + "");
            Log.i("deleted", this.coverPath.delete() + "");
            Log.i("exists", this.coverPath.exists() + "");
        }
    }

    public void onStart(){
        super.onStart();
        setUpDJInfo();
        setUpStatusBar();
        //
        djBio.setText(AppUtils.removeHTMLJunk(bio));
        setUpSupportBar();
        playlistView.setAdapter(new PlaylistListAdapter(this.playlist));
    }

    private void setUpSupportBar(){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(this.djName);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(coverPhotoColor));
            Spannable text = new SpannableString(getSupportActionBar().getTitle());
            text.setSpan(new ForegroundColorSpan(AppUtils.ContrastColor(coverPhotoColor)), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(text);
        }
    }

    private void setUpDJInfo(){
        if(coverPhoto != null) {
            TextView playlistLabel = (TextView)findViewById(R.id.DJPlaylistLabel);
            djImage.setImageBitmap(coverPhoto);
            bio = AppUtils.removeHTMLJunk(bio);
            //
            coverPhotoColor = AppUtils.averageColor(coverPhoto);
            PercentRelativeLayout sV = (PercentRelativeLayout) findViewById(R.id.DJInfoLayout);
            sV.setBackground(new ColorDrawable(coverPhotoColor));
            textColor = AppUtils.ContrastColor(coverPhotoColor);
            djBio.setTextColor(textColor);
            djShowName.setTextColor(textColor);
            playlistLabel.setBackground(new ColorDrawable(coverPhotoColor));
            playlistLabel.setTextColor(textColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                (findViewById(R.id.DJInfoLayout)).setElevation(15f);
            }
        }
    }

    private void setUpStatusBar(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(AppUtils.darkenColor(coverPhotoColor));
    }

    private class PlaylistListAdapter extends BaseExpandableListAdapter {

        TreeMap<String,LinkedList<SerializableTrack>> playlist;
        ArrayList<String> dates;

        public PlaylistListAdapter(TreeMap<String,LinkedList<SerializableTrack>> playlist) {
            this.playlist = playlist;
            dates = new ArrayList<>(playlist.keySet());
        }

        public Object getChild(int groupPosition, int childPosition) {
            return playlist.get(dates.get(groupPosition)).get(childPosition);
        }

        public View getChildView(final int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final SerializableTrack serializableTrack = (SerializableTrack) getChild(groupPosition, childPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.playlist_child, null);
            }
            TextView txtListChild = (TextView) convertView.findViewById(R.id.trackChild);
            txtListChild.setText(serializableTrack.getTrackName());

            ImageView info = (ImageView) convertView.findViewById(R.id.djTrackInfo);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastGroup = groupPosition;
                   DJTrackDialog dialog = DJTrackDialog.newInstance(serializableTrack);
                    dialog.show(fragmentManager, "SerializableTrack");
                }
            });

            return convertView;
        }

        public int getChildrenCount(int groupPosition) {
            return playlist.get(dates.get(groupPosition)).size();
        }

        public long getChildId(int group,int child){
            return playlist.get(dates.get(group)).get(child).getId();
        }

        public Object getGroup(int groupPosition) {
            return dates.get(groupPosition);
        }

        public int getGroupCount() {
            return dates.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String date = (String) getGroup(groupPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.playlist_group,
                        null);
            }
            TextView item = (TextView) convertView.findViewById(R.id.playlistGroup);
            item.setTypeface(null, Typeface.BOLD);
            item.setText(date);

            return convertView;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}
