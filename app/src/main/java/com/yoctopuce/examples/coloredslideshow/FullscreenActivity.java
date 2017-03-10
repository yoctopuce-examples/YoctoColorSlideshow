package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity
{
    private static final String IMAGE_PARAM = "IMAGE_PARAM";
    private ThumbnailDownloader<ViewSwitcher> mThumbnailDownloader;
    private ArrayList<String> _imgUrls= new ArrayList<>();
    private int _pos = -1;
    private ViewSwitcher _switcher;
    private ImageView _imageViewA;
    private ImageView _imageViewB;


    public static Intent intentWithParams(Context context, String imageURl)
    {
        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra(IMAGE_PARAM, imageURl);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        new DownloadImageList(this)
        {
            @Override
            void updateResult(ArrayList<String> result)
            {
                _imgUrls = result;
                _pos = 0;
                updatePicture();
            }

            @Override
            void onError(String msg)
            {
                Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_LONG).show();
            }
        }.execute();


        _switcher = (ViewSwitcher) findViewById(R.id.switcher);

        _switcher.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        _switcher.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                updatePicture();
            }
        });


        _imageViewA = (ImageView) findViewById(R.id.img_a);
        _imageViewB = (ImageView) findViewById(R.id.img_b);


        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(this, responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<ViewSwitcher>()
                {
                    @Override
                    public void onThumbnailDownloaded(ViewSwitcher switcher, Bitmap bitmap)
                    {
                        if (switcher.getDisplayedChild() == 0) {
                            _imageViewB.setImageBitmap(bitmap);
                            switcher.showNext();
                        } else {
                            _imageViewA.setImageBitmap(bitmap);
                            switcher.showPrevious();
                        }


                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();

        updatePicture();
    }

    private void updatePicture()
    {
        _pos++;
        if (_pos >= _imgUrls.size()) {
            _pos = 0;
        }
        if (_pos < _imgUrls.size()) {
            final String url = _imgUrls.get(_pos);
            mThumbnailDownloader.queueDisplay(_switcher, url);
        }
    }


}
