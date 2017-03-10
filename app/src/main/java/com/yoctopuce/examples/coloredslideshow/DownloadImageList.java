package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;

abstract class DownloadImageList extends AsyncTask<Void, Void, ArrayList<String>>
{
    private final Context _context;
    private String _errmsg;

    public DownloadImageList(Context context)
    {
        _context = context.getApplicationContext();
    }

    protected ArrayList<String> doInBackground(Void... params)
    {
        try {
            return MiscHelpers.getPictureList(_context);
        } catch (IOException e) {
            e.printStackTrace();
            _errmsg = e.getLocalizedMessage();
        }
        return null;
    }

    protected void onPostExecute(ArrayList<String> result)
    {
        if (result != null) {
            updateResult(result);
        } else if (_errmsg != null) {
            onError(_errmsg);
        }
    }

    abstract void updateResult(ArrayList<String> result);

    abstract void onError(String msg);

}
