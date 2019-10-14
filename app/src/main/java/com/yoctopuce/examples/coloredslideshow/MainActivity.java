package com.yoctopuce.examples.coloredslideshow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{


    private RecyclerView _recyclerView;
    private List<String> _imgUrls = new ArrayList<>();
    private ThumbnailDownloader<PictureHolder> mThumbnailDownloader;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        _recyclerView = (RecyclerView) findViewById(R.id.photo_recycler_view);
        _recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(this, responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PictureHolder>()
                {
                    @Override
                    public void onThumbnailDownloaded(PictureHolder photoHolder, Bitmap bitmap)
                    {
                        photoHolder.bindDrawable(bitmap);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();


        setupAdapter();
        new DownloadImageList(MainActivity.this)
        {
            @Override
            void updateResult(ArrayList<String> result)
            {
                _imgUrls = result;
                setupAdapter();

            }

            @Override
            void onError(String msg)
            {
                Snackbar.make(_recyclerView, msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        }.execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(FullscreenActivity.intentWithParams(MainActivity.this, null));
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupAdapter()
    {
        _recyclerView.setAdapter(new PhotoAdapter(_imgUrls));
    }

    private class PictureHolder extends RecyclerView.ViewHolder
    {
        private ImageView mItemImageView;

        public PictureHolder(View itemView)
        {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Bitmap bitmap)
        {
            mItemImageView.setImageBitmap(bitmap);
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PictureHolder>
    {

        private List<String> _AdapterItems;

        PhotoAdapter(List<String> picturesItems)
        {
            _AdapterItems = picturesItems;
        }

        @Override
        public PictureHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.picture_item, viewGroup, false);
            return new PictureHolder(view);
        }

        @Override
        public void onBindViewHolder(final PictureHolder pictureHolder, int position)
        {
            final String imageUrl = _AdapterItems.get(position);
            pictureHolder.mItemImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mThumbnailDownloader.queueDisplay(pictureHolder, imageUrl);
                }
            });
            mThumbnailDownloader.queueThumbnail(pictureHolder, imageUrl);
        }

        @Override
        public int getItemCount()
        {
            return _AdapterItems.size();
        }
    }

}
