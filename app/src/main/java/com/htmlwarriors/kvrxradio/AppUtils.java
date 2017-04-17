package com.htmlwarriors.kvrxradio;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by Daniel on 3/3/2016.
 * <p/>
 * :)
 */
public class AppUtils {


    public static String removeHTMLJunk(String title){
        while (isThereTrash(title))
            title = AppUtils.fixTitle(title);
        return title;
    }

    private static String fixTitle(String title){
        if (isThereTrash(title)){
            String code = title.substring(title.indexOf("&"), title.indexOf(";") + 1);
            title = title.replace(code, convertCode(code));
            return title;
        }
        else
            return title;
    }

    private static boolean isThereTrash(String title){
        return title.contains("&#") || title.contains("&amp;") ||
                title.contains("&nbsp;");
    }

    private static String convertCode(String code){

        switch (code){
            case "&nbsp;": return " ";
            case "&amp;" : return "&";
            case "&#032;" : return " ";
            case "&#033;" : return "!";
            case "&#034;" : return "\"";
            case "&#035;" : return "#";
            case "&#036;" : return "$";
            case "&#037;" : return "%";
            case "&#038;" : return "&";
            case "&#039;" : return "'";
            case "&#040;" : return "(";
            case "&#041;" : return ")";
            case "&#042;" : return "*";
            case "&#043;" : return "+";
            case "&#044;" : return ",";
            case "&#045;" : return "-";
            case "&#046;" : return ".";
            case "&#047;" : return "/";
            case "&#048;" : return "0";
            case "&#049;" : return "1";
            case "&#050;" : return "2";
            case "&#051;" : return "3";
            case "&#052;" : return "4";
            case "&#053;" : return "5";
            case "&#054;" : return "6";
            case "&#055;" : return "7";
            case "&#056;" : return "8";
            case "&#057;" : return "9";
            case "&#058;" : return ":";
            case "&#059;" : return ";";
            case "&#060;" : return "<";
            case "&#061;" : return "=";
            case "&#062;" : return ">";
            case "&#063;" : return "?";
            case "&#064;" : return "@";
            case "&#091;" : return "[";
            case "&#092;" : return "\\";
            case "&#093;" : return "]";
            case "&#094;" : return "^";
            case "&#095;" : return "_";
            case "&#096;" : return "`";
            case "&#123;" : return "{";
            case "&#128;" : return "?";
            case "&#130;" : return ",";
            case "&#133;" : return "...";
            case "&#147;" : return "\"";
        }
        return "";
    }

    public static int ContrastColor(int color) {
        int d = 0;

        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - ( 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))/255;

        if (a < 0.5)
            return Color.BLACK; // bright colors - black font

          return Color.WHITE;// dark colors - white font



    }
    
    /*
        lightenColor sets up the color of the background
        that is behind the album cover. LightenColor
        grabs the color of the middle pixel, lightens it,
        and then sets it as the color of the background.
     */
    public static int lightenColor(int color){


        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 0.2f + 0.8f * hsv[2];// value component
        return  Color.HSVToColor(hsv);

    }


    public static int darkenColor(int color){


        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f; // value component
        color = Color.HSVToColor(hsv);


        return color;
    }

    public static int averageColor(Bitmap bitmap){
        long redBucket = 0;
        long greenBucket = 0;
        long blueBucket = 0;
        long pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++)
        {
            for (int x = 0; x < bitmap.getWidth(); x++)
            {
                int c = bitmap.getPixel(x, y);

                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
            }
        }


        return Color.rgb((int)(redBucket / pixelCount),
                (int)(greenBucket / pixelCount),
                (int)(blueBucket / pixelCount));

    }

    public static TreeMap<String, LinkedList<SerializableTrack>> toTreeMap(HashMap<String, ArrayList<SerializableTrack>> hashMap) {

        TreeMap<String, LinkedList<SerializableTrack>> map = new TreeMap<>(new DateComparator());
        for(String s : hashMap.keySet()){
            LinkedList<SerializableTrack> linkedListToArrayList= new LinkedList(hashMap.get(s));
            map.put(s,linkedListToArrayList);
        }
        return map;
    }
}
