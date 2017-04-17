package com.htmlwarriors.kvrxradio;

import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Daniel on 3/25/2016.
 * <p/>
 * :)
 */
public class DateComparator implements Comparator<String> {


    private int getMonthVal(String month){
        switch(month){
            case "january":
                return 0;
            case "february":
                return 1;
            case "march":
                return 2;
            case "april":
                return 3;
            case "may":
                return 4;
            case "june":
                return 5;
            case "july":
                return 6;
            case "august":
                return 7;
            case "september":
                return 8;
            case "october":
                return 9;
            case "november":
                return 10;
            default:
                return 11;

        }
    }

    @Override
    public int compare(String lhs, String rhs) {
        Scanner dateReader = new Scanner(lhs);

        String monthLHS = dateReader.next().trim();
        dateReader.useDelimiter(",");
        int dayLHS = Integer.parseInt(dateReader.next().trim());
        dateReader.useDelimiter(" ");
        dateReader.next();
        int yearLHS = Integer.parseInt(dateReader.next().trim());

        dateReader = new Scanner(rhs);
        String monthRHS = dateReader.next().trim();
        dateReader.useDelimiter(",");
        int dayRHS = Integer.parseInt(dateReader.next().trim());
        dateReader.useDelimiter(" ");
        dateReader.next();
        int yearRHS = Integer.parseInt(dateReader.next().trim());

        if(yearLHS != yearRHS){
            return yearRHS - yearLHS;
        }else if(!monthLHS.equals(monthRHS)){
            return getMonthVal(monthRHS.toLowerCase())- getMonthVal(monthLHS.toLowerCase());
        }else{
            return dayRHS - dayLHS;
        }

    }

    @Override
    public boolean equals(Object object) {
       return true;
    }
}
