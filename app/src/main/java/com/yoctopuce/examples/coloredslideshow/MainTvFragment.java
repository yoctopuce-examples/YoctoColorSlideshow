package com.yoctopuce.examples.coloredslideshow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.app.HeadersFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainTvFragment extends BrowseFragment
{
    private static final String TAG = "MainFragment";
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;


    ImagePresenter _imagePresenter;
    private ArrayObjectAdapter _listRowAdapter;
    private List<String> _imgUrls = new ArrayList<>();
    private ThumbnailDownloader<ImagePresenter.ViewHolder> mThumbnailDownloader;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(getActivity(), responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<ImagePresenter.ViewHolder>()
                {
                    @Override
                    public void onThumbnailDownloaded(ImagePresenter.ViewHolder photoHolder, Bitmap bitmap)
                    {
                        photoHolder.setBitmap(bitmap);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        new DownloadImageList(getActivity())
        {
            @Override
            void updateResult(ArrayList<String> result)
            {
                _imgUrls = result;
                for (String url : _imgUrls) {
                    _listRowAdapter.add(url);
                }
                //setupAdapter();
            }

            @Override
            void onError(String msg)
            {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

            }
        }.execute();

        prepareBackgroundManager();
        setupUIElements();
        loadRows();
        setupEventListeners();


    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private void loadRows()
    {

        ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());


        _imagePresenter = new ImagePresenter(mThumbnailDownloader);

        _listRowAdapter = new ArrayObjectAdapter(_imagePresenter);
        HeaderItem gridHeader = new HeaderItem(0, "Pictures");
        mRowsAdapter.add(new ListRow(gridHeader, _listRowAdapter));

        HeaderItem prefHeader = new HeaderItem(1, "Preference");
        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);

        mRowsAdapter.add(new ListRow(prefHeader, gridRowAdapter));


        final HeadersFragment headersFragment = getHeadersFragment();
        headersFragment.setOnHeaderClickedListener(new HeadersFragment.OnHeaderClickedListener()
        {
            @Override
            public void onHeaderClicked(RowHeaderPresenter.ViewHolder viewHolder, Row row)
            {
                Log.d(TAG, row.toString());
                if (row.getId() == 1) {
                    startActivity(new Intent(getActivity(), SettingsTvActivity.class));
                }
            }
        });

        setAdapter(mRowsAdapter);

    }

    private void prepareBackgroundManager()
    {

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        DisplayMetrics mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements()
    {
        setTitle(getString(R.string.app_name)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);

        // set fastLane (or headers) background color
        //todo: setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
    }

    private void setupEventListeners()
    {
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener
    {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row)
        {
            if (item instanceof String) {
                String url = (String) item;
                startActivity(FullscreenActivity.intentWithParams(getActivity(), url));
            }
        }
    }


    private class GridItemPresenter extends Presenter
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent)
        {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item)
        {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder)
        {
        }
    }

}
