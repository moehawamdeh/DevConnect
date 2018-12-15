package org.ieeemadc.devconnect.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.ieeemadc.devconnect.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class DevConnectUtils implements Comparator {

    public static String longFormatter(Long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return longFormatter(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + longFormatter(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String joinString(String s, List<String> keywords) {
        StringBuilder builder=new StringBuilder();
        for(String word:keywords){
            builder.append(word).append(s);
        }
        return builder.toString();
    }

    public static Long stringToUnixTimeStamp(Context context,String time) {
        Date now = Calendar.getInstance().getTime();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(now);
        if( time==null || time.equals(context.getResources().getString(R.string.anytime)))
            return -1L;
        else if(time.equals(context.getResources().getString(R.string.past_hour)))
            calendar.add(Calendar.HOUR,-1);
        else if(time.equals(context.getResources().getString(R.string.past_day)))
            calendar.add(Calendar.DAY_OF_YEAR,-1);
        else if(time.equals(context.getResources().getString(R.string.past_week)))
            calendar.add(Calendar.DAY_OF_YEAR,-7);
        else if(time.equals(context.getResources().getString(R.string.past_month)))
            calendar.add(Calendar.MONTH,-1);
        else if(time.equals(context.getResources().getString(R.string.past_year)))
            calendar.add(Calendar.YEAR,-1);
        return calendar.getTimeInMillis() /1000L;


    }

    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }
    public static void showSnackBar(View view, String msg, String action, View.OnClickListener listener){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        if(!action.isEmpty() ||listener!=null)
            snackbar = snackbar.setAction(action, listener);
        snackbar.show();
    }
    public static void showAlertDialog(Context context, String title, String message, String positiveButton, String negativeButton, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveButton, listener)
                .setNegativeButton(negativeButton, listener)
                .create()
                .show();
    }
    public static void showAlertDialog(Context context, String title, String message, String positiveButton, DialogInterface.OnClickListener listener) {
        String button= positiveButton.equals("")?context.getString(android.R.string.ok):positiveButton;
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(button, listener)
                .create()
                .show();
    }
    public static int dpToPx(Context context,int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
    public static void setDrawableColor(Context context,Drawable drawable,@ColorRes int color_id){
        if (drawable instanceof ShapeDrawable) {
            ((ShapeDrawable)drawable).getPaint().setColor(ContextCompat.getColor(context,R.color.colorPrimary));
        } else if (drawable instanceof GradientDrawable) {
            ((GradientDrawable)drawable).setColor(ContextCompat.getColor(context,R.color.colorPrimary));
        } else if (drawable instanceof ColorDrawable) {
            ((ColorDrawable)drawable).setColor(ContextCompat.getColor(context,R.color.colorPrimary));
        }

    }
    public static Drawable resizeDrawable(Context context,Drawable drawable,int width,int height){
        int topBottom= dpToPx(context,height),rightLeft= dpToPx(context,width);
        drawable.setBounds(0,0,rightLeft,topBottom);
        return drawable;
    }
    public static void resizeView(Context context,int width,int height){}
    public static String listToText(List<String>list){
        StringBuilder result= new StringBuilder();
        for(int i=0;i<list.size();i++)
        {
            result.append("- ").append(list.get(i));
            if(i!=list.size()-1)
                result.append("\n");
        }
        return result.toString();
    }
    public static String timeStampToString(com.google.firebase.Timestamp timestamp,Context context){
        if(timestamp == null)
            return context.getResources().getString(R.string.date_not_specified);
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return dateFormat.format(timestamp.toDate());
    }
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }
}
