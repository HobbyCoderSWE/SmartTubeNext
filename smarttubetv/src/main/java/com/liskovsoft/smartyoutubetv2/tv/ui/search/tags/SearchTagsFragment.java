package com.liskovsoft.smartyoutubetv2.tv.ui.search.tags;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.search.MediaServiceSearchTagProvider;
import com.liskovsoft.smartyoutubetv2.common.app.models.search.vineyard.Tag;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.SearchPresenter;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.adapter.VideoGroupObjectAdapter;
import com.liskovsoft.smartyoutubetv2.tv.ui.common.LeanbackActivity;
import com.liskovsoft.smartyoutubetv2.tv.ui.search.tags.vineyard.SearchTagsFragmentBase;

public class SearchTagsFragment extends SearchTagsFragmentBase {
    private static final String TAG = SearchTagsFragment.class.getSimpleName();
    private SearchPresenter mSearchPresenter;
    private VideoGroupObjectAdapter mItemResultsAdapter;
    private String mSearchQuery;
    private int mNewQueryLength;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchPresenter = SearchPresenter.instance(getContext());
        mSearchPresenter.register(this);
        mItemResultsAdapter = new VideoGroupObjectAdapter();

        setItemResultsAdapter(mItemResultsAdapter);
        setSearchTagsProvider(new MediaServiceSearchTagProvider());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearchPresenter.onInitDone();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSearchPresenter.unregister(this);
    }

    @Override
    public void updateSearch(VideoGroup group) {
        mItemResultsAdapter.append(group);
    }

    @Override
    public void clearSearch() {
        mItemResultsAdapter.clear();
    }

    @Override
    public void startSearch(String searchText) {
        if (searchText != null) {
            setSearchQuery(searchText, true);
        } else {
            loadSearchTags("");
            startRecognition();
        }

        if (getRowsSupportFragment() != null) {
            // Move selection to the top
            getRowsSupportFragment().setSelectedPosition(0);
        }
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        loadSearchTags(newQuery);

        if (isNewQueryChanged(newQuery)) {
            loadSearchResult(newQuery);
        }

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadSearchResult(query);
        return true;
    }

    private void loadSearchResult(String searchQuery) {
        if (!TextUtils.isEmpty(searchQuery) && !searchQuery.equals(mSearchQuery)) {
            mSearchQuery = searchQuery;
            mSearchPresenter.onSearch(searchQuery);
        }
    }

    private boolean isNewQueryChanged(String searchQuery) {
        if (TextUtils.isEmpty(searchQuery)) {
            mNewQueryLength = 0;
            return false;
        }

        int newLength = searchQuery.length();
        boolean isChanged = Math.abs(newLength - mNewQueryLength) != 1;
        mNewQueryLength = newLength;

        return isChanged;
    }

    public void focusOnSearch() {
        getView().findViewById(R.id.lb_search_bar).requestFocus();
    }

    @Override
    protected void onItemViewClicked(Object item) {
        if (item instanceof Video) {
            if (getActivity() instanceof LeanbackActivity) {
                boolean longClick = ((LeanbackActivity) getActivity()).isLongClick();
                Log.d(TAG, "Is long click: " + longClick);

                if (longClick) {
                    mSearchPresenter.onVideoItemLongClicked((Video) item);
                } else {
                    mSearchPresenter.onVideoItemClicked((Video) item);
                }
            }
        } else if (item instanceof Tag) {
            startSearch(((Tag) item).tag);
        }
    }

    @Override
    protected void onItemViewSelected(Object item) {
        if (item instanceof Video) {
            checkScrollEnd((Video) item);
        }
    }

    private void checkScrollEnd(Video item) {
        int size = mItemResultsAdapter.size();
        int index = mItemResultsAdapter.indexOf(item);

        if (index > (size - 4)) {
            mSearchPresenter.onScrollEnd(mItemResultsAdapter.getGroup());
        }
    }
}
