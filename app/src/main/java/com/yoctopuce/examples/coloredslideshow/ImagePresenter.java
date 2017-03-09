package com.yoctopuce.examples.coloredslideshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by Yoctopuce on 07.03.2017.
 */

public class ImagePresenter extends Presenter
{

    private static final String TAG = ImagePresenter.class.getSimpleName();
    private static Context mContext;
    private final static int CARD_HEIGHT = 176;
    private final static int CARD_WIDTH = 313;
    private final ThumbnailDownloader<ViewHolder> _downloader;

    public ImagePresenter(ThumbnailDownloader<ViewHolder> mThumbnailDownloader)
    {
        _downloader = mThumbnailDownloader;
    }


    static class ViewHolder extends Presenter.ViewHolder
    {
        private String _url;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;

        public ViewHolder(ImageCardView view)
        {
            super(view);
            mCardView = view;
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.banner);
        }

        public void setImageUrl(String url)
        {
            _url = url;
            mCardView.setTitleText(_url);
        }

        public ImageCardView getCardView()
        {
            return mCardView;
        }

        public Drawable getDefaultCardImage()
        {
            return mDefaultCardImage;
        }

        public void setBitmap(Bitmap bitmap)
        {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mCardView.setMainImage(bitmapDrawable, true);
        }
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent)
    {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item)
    {
        String url = (String) item;
        Log.d(TAG, "onBindViewHolder:" + url);
        ((ViewHolder) viewHolder).setImageUrl(url);
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        _downloader.queueThumbnail((ViewHolder) viewHolder, url);

    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder)
    {
    }
}
