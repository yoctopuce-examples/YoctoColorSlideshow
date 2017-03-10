package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YColorLedCluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yoctopuce on 06.03.2017.
 */

public class MiscHelpers
{


    public static ArrayList<String> getPictureList(Context context) throws IOException
    {
        final String img_url = PreferenceManager.getDefaultSharedPreferences(context).getString("img_url", context.getString(R.string.pref_default_img_list));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        URL url = new URL(img_url);
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();
        InputStream in = connection.getInputStream();
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            final String responseMessage = connection.getResponseMessage();
            Log.d("dbg", responseMessage);
            throw new IOException(responseMessage);
        }
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        byte[] bytes = out.toByteArray();
        String txt = new String(bytes);
        String[] split = txt.split("\n");
        return new ArrayList<>(Arrays.asList(split));
    }


    public static Bitmap loadBitmap(String url) throws IOException
    {
        InputStream in = new java.net.URL(url).openStream();
        return BitmapFactory.decodeStream(in);
    }

    public static int setColorLeds(Context context, Bitmap bitmap) throws YAPI_Exception
    {
        Palette p = Palette.from(bitmap).generate();
        int dominantColor = p.getDominantColor(0);
        final String hub_url = PreferenceManager.getDefaultSharedPreferences(context).getString("yocto_hub", context.getString(R.string.pref_default_yocto_hub));
        YAPI.EnableUSBHost(context);
        YAPI.RegisterHub(hub_url);
        YColorLedCluster leds = YColorLedCluster.FirstColorLedCluster();
        while (leds != null) {
            final int activeLedCount = leds.get_activeLedCount();
            leds.rgb_move(0, activeLedCount, dominantColor & 0xffffff, 1000);
            leds = leds.nextColorLedCluster();
        }
        YAPI.FreeAPI();
        return dominantColor;
    }


}
