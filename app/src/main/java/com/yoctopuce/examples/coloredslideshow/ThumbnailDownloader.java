package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.yoctopuce.YoctoAPI.YAPI_Exception;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread
{
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_DISPLAY = 1;
    private final Context _context;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;



    public interface ThumbnailDownloadListener<T>
    {
        void onThumbnailDownloaded(T target, Bitmap bitmap);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener)
    {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Context context, Handler responseHandler)
    {
        super(TAG);
        mResponseHandler = responseHandler;
        _context = context.getApplicationContext();
    }

    @Override
    protected void onLooperPrepared()
    {
        mRequestHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target, false);
                } else if (msg.what == MESSAGE_DISPLAY) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target, true);

                }
            }
        };
    }

    @Override
    public boolean quit()
    {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url)
    {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }
    public void queueDisplay(T target, String url)
    {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DISPLAY, target)
                    .sendToTarget();
        }
    }

    public void clearQueue()
    {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestHandler.removeMessages(MESSAGE_DISPLAY);
        mRequestMap.clear();
    }

    private void handleRequest(final T target, boolean setColorLed)
    {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            final Bitmap bitmap = MiscHelpers.loadBitmap(url);
            Log.i(TAG, "Bitmap created");
            if (setColorLed) {
                try {
                    MiscHelpers.setColorLeds(_context,bitmap);
                } catch (YAPI_Exception e) {
                    e.printStackTrace();
                }
            }

            mResponseHandler.post(new Runnable()
            {
                public void run()
                {
                    if (mRequestMap.get(target) != url ||
                            mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
