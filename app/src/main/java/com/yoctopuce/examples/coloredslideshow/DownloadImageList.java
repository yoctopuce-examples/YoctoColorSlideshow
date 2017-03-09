package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

abstract class DownloadImageList extends AsyncTask<Void, Void, ArrayList<String>>
{
    private final Context _context;

    public DownloadImageList(Context context)
    {
        _context = context;
    }

    protected ArrayList<String> doInBackground(Void... params)
    {
        try {
            return MiscHelpers.getPictureList(_context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(ArrayList<String> result)
    {
        if (result != null) {
            updateResult(result);
        }
    }

    abstract void updateResult(ArrayList<String> result);

}
