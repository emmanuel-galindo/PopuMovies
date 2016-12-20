package com.popumovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.popumovies.adapter.ReviewAdapter;
import com.popumovies.adapter.VideoAdapter;
import com.popumovies.data.MovieContract;
import com.popumovies.data.MovieContract.MovieEntry;
import com.popumovies.utils.HelperOkHttpClient;
import com.popumovies.utils.VarColumnGridLayoutManager;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by manu on 12/2/15.
 */
public class DetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final int FRAGMENT_DETAIL_MOVIE_LOADER = 1;
    private static final int FRAGMENT_DETAIL_VIDEO_LOADER = 2;
    private static final int FRAGMENT_DETAIL_REVIEW_LOADER = 3;
    public static final String DETAIL_URI = "URI";
    public static final String DETAIL_MOVIEID = "_id";
    public static final String DETAIL_TWOPANE = "TWO_PANE";


    //    static final String[] FORECAST_COLUMNS = {
//            MovieContract.MovieEntry._ID,
//            MovieContract.MovieEntry.COLUMN_TMDB_ID,
//            MovieContract.MovieEntry.COLUMN_TITLE,
//            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
//            MovieContract.MovieEntry.COLUMN_OVERVIEW,
//            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
//            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
//            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
//            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
//            MovieContract.MovieEntry.COLUMN_POPULARITY
//    };
    private View mRootView;
    private Uri mUri;
    private TextView mTitleView;
    private ImageView mPosterView;
    private TextView mYearView;
    private TextView mDurationView;
//    private TextView mPopularityView;
    private TextView mDescriptionView;
    private TextView mVoteAverageView;
    private RatingBar mVoteAverageRatingBar;
    private ImageButton mStarButton;
    private String mTitle;
    private ExpandableListView mTrailersListView;
    private RecyclerView mRecyclerViewVideo;
    private VideoAdapter mVideoAdapter;
    private RecyclerView mRecyclerViewReview;
    private ReviewAdapter mReviewAdapter;
    private GridLayoutManager mLayoutManager;
    private LinearLayoutManager mReviewLayoutManager;
    private long mMovieId;
    private String mOriginalTitle;
    private TextView titleView;
    private ImageButton starButton;
    private TextView descriptionView;
    private RatingBar voteAverageRatingBar;
    private TextView voteAverageView;
    private TextView yearView;
    private ImageView posterView;
    private boolean mTwoPane = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


//        Bundle arguments = getArguments();
//        if (arguments != null) {
//            mUri = arguments.getParcelable("Uri");
//        }
//        mTwoPane = (getActivity().findViewById(R.id.movie_list_container) != null);

        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);

        titleView = (TextView) mRootView.findViewById(R.id.textview_movie_title);
        posterView = (ImageView) mRootView.findViewById(R.id.imageview_movie_poster);
        yearView = (TextView) mRootView.findViewById(R.id.textview_movie_year);
//        mDurationView = (TextView) mRootView.findViewById(R.id.textview_movie_duration);
//        mPopularityView = (TextView) mRootView.findViewById(R.id.textview_movie_popularity);
        voteAverageView = (TextView) mRootView.findViewById(R.id.textview_movie_vote_average);
        voteAverageRatingBar = (RatingBar) mRootView.findViewById(R.id.ratingbar_movie_vote_average);
        descriptionView = (TextView) mRootView.findViewById(R.id.textview_movie_description);
        starButton = (ImageButton) mRootView.findViewById(R.id.button_star_favorite);

        mRecyclerViewVideo = (RecyclerView) mRootView.findViewById(R.id.recyclerview_video);
//        mRecyclerViewVideo.setMinimumHeight(300);
//        mRecyclerViewVideo.setHasFixedSize(true);
//        mLayoutManager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
        mLayoutManager = new VarColumnGridLayoutManager(getActivity(), 200);

        mRecyclerViewVideo.setLayoutManager(mLayoutManager);
//        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);
//        mRecyclerViewVideo.addItemDecoration(itemDecoration);
        mVideoAdapter = new VideoAdapter(getActivity(), null, 0);
        mRecyclerViewVideo.setAdapter(mVideoAdapter);

//        getLoaderManager().initLoader(FRAGMENT_DETAIL_VIDEO_LOADER, null, this);

        mRecyclerViewReview = (RecyclerView) mRootView.findViewById(R.id.recyclerview_review);
        mRecyclerViewReview.setHasFixedSize(true);
        mReviewLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewReview.setLayoutManager(mReviewLayoutManager);
        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
        mRecyclerViewReview.setAdapter(mReviewAdapter);

//        getLoaderManager().initLoader(FRAGMENT_DETAIL_REVIEW_LOADER, null, this);

        Bundle arguments = getArguments();
        // if arguments is not null, it came from the pageradapter from detailactivity
        if (arguments != null) {
            if (arguments.containsKey(DETAIL_TWOPANE))
                //TODO: why not to do it from the getResources files
                mTwoPane = arguments.getBoolean(DETAIL_TWOPANE);
            if (arguments.size() > 1) {
                //mMovieId and mUri are loaded here just for single panel & pageradapter.
                // In twopane these are created after onItemSelected (for initial and actual click)
                mMovieId = Long.parseLong(arguments.getString(DETAIL_MOVIEID));
                mUri = MovieEntry.buildSingleMovieUri(mMovieId);
                // if two pane, the movie data needs to be retrieved from db, and then call fillForm
                if (mTwoPane) {
                    getLoaderManager().initLoader(FRAGMENT_DETAIL_MOVIE_LOADER, null, this);
                } else {
                    fillForm(arguments);
                }
            }
        }



//        titleView.setText(mOriginalTitle);
//
//        Date release_date = new Date(Long.parseLong(
//                arguments.getString(MovieEntry.COLUMN_RELEASE_DATE)));
//        // extract the year
//        SimpleDateFormat df = new SimpleDateFormat("yyyy");
//        yearView.setText(df.format(release_date));
//
//        // Poster image is composed of 3 parts
//        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
//        // 2 - Then you will need a ‘size’, which will be one of the following:
//        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
//        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
////        String poster = "http://image.tmdb.org/t/p/w500"+arguments.getString(MovieEntry.COLUMN_POSTER_PATH);
//        String poster = "http://image.tmdb.org/t/p/w185"+arguments.getString(MovieEntry.COLUMN_POSTER_PATH);
//        Log.d(LOG_TAG, "Loading poster url => " + poster);
////        OkHttpClient okHttpClient = HelperOkHttpClient.getOkHttpClientBuilder().build();
////        Picasso picasso = new Picasso.Builder(mRootView.getContext())
////                .downloader(new OkHttp3Downloader(okHttpClient))
////                .build();
//        Picasso picasso = HelperOkHttpClient.getPicassoInstance(mRootView.getContext());
//        picasso.load(poster)
//                .into(posterView);
////        Picasso picasso = Picasso.with(mRootView.getContext());
////        picasso.setIndicatorsEnabled(true);
////        picasso.load(poster).into(posterView);
//        posterView.setContentDescription(arguments.getString(MovieEntry.COLUMN_TITLE));
//
//        String voteAverage = arguments.getString(MovieEntry.COLUMN_VOTE_AVERAGE);
//        voteAverageView.setText(voteAverage + getString(R.string.vote_avg_string));
//
//        float voteAverageNumber = Float.parseFloat(voteAverage) / 2;
//        voteAverageRatingBar.setRating(voteAverageNumber);
//
//        descriptionView.setText(arguments.getString(MovieEntry.COLUMN_OVERVIEW));
//
//        starButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Do something in response to button click
//                //mUri = content://com.popumovies/movie/4
//                onClickFavorite(v, mUri);
//            }
//        });
//        // Select star if favorite
//        starButton.setSelected((arguments.getString(MovieEntry.COLUMN_FAVORITE).equals("1")));
//
//        Button favButton = (Button) mRootView.findViewById(R.id.button_favorite);
//        favButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Do something in response to button click
//                //mUri = content://com.popumovies/movie/4
//                ImageButton starButton = (ImageButton)
//                        mRootView.findViewById(R.id.button_star_favorite);
//                onClickFavorite(starButton,mUri);
//            }
//        });
//
//        mRecyclerViewVideo = (RecyclerView) mRootView.findViewById(R.id.recyclerview_video);
////        mRecyclerViewVideo.setMinimumHeight(300);
////        mRecyclerViewVideo.setHasFixedSize(true);
////        mLayoutManager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
//        mLayoutManager = new VarColumnGridLayoutManager(getActivity(), 200);
//
//        mRecyclerViewVideo.setLayoutManager(mLayoutManager);
////        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);
////        mRecyclerViewVideo.addItemDecoration(itemDecoration);
//        mVideoAdapter = new VideoAdapter(getActivity(), null, 0);
//        mRecyclerViewVideo.setAdapter(mVideoAdapter);
//
//        getLoaderManager().initLoader(FRAGMENT_DETAIL_VIDEO_LOADER, null, this);
//
//
//        mRecyclerViewReview = (RecyclerView) mRootView.findViewById(R.id.recyclerview_review);
//        mRecyclerViewReview.setHasFixedSize(true);
//        mReviewLayoutManager = new LinearLayoutManager(getActivity());
//        mRecyclerViewReview.setLayoutManager(mReviewLayoutManager);
//        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
//        mRecyclerViewReview.setAdapter(mReviewAdapter);
//
//        getLoaderManager().initLoader(FRAGMENT_DETAIL_REVIEW_LOADER, null, this);


//        start good
//        mTrailersListView = (ExpandableListView)
//                mRootView.findViewById(R.id.expandable_listview_movie_trailers);
//        mTrailersListView.setTranscriptMode(0);
//
//        // Set up our adapter
//        String group[] = {"Trailers" , "Reviews"};
//        String[][] child = { { "John", "Bill" }, { "Alice", "David" } };
//
//
//        final String NAME = "SECTION";
//
//        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
//        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
//        for (int i = 0; i < group.length; i++) {
//            Map<String, String> curGroupMap = new HashMap<String, String>();
//            groupData.add(curGroupMap);
//            curGroupMap.put(NAME, group[i]);
//
//            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
//            for (int j = 0; j < child[i].length; j++) {
//                Map<String, String> curChildMap = new HashMap<String, String>();
//                children.add(curChildMap);
//                curChildMap.put(NAME, child[i][j]);
//            }
//            childData.add(children);
//        }
//        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(getActivity(),
//                groupData, android.R.layout.simple_expandable_list_item_1,
//                new String[] { "SECTION"}, new int[] { android.R.id.text1 },
//                childData, android.R.layout.simple_expandable_list_item_2,
//                new String[] { "SECTION"}, new int[] { android.R.id.text1 });
//
//        mTrailersListView.setAdapter(adapter);
//      end good

//        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
//        Map<String, String> curGroupMap = new HashMap<String, String>();
//        curGroupMap.put("SECTION", "Trailers");
//        groupData.add(curGroupMap);
//        curGroupMap = new HashMap<String, String>();
//        curGroupMap.put("SECTION", "Reviews");
//        groupData.add(curGroupMap);
//
//
//        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
//        List<Map<String, String>> children = new ArrayList<Map<String, String>>();
//        Map<String, String> curChildMap = new HashMap<String, String>();
//        children.add(curChildMap);
//        curChildMap.put("SECTION", "");
//        curChildMap = new HashMap<String, String>();
//        curChildMap.put("SECTION", "");
//        childData.add(children);




//        ArrayList sections = new ArrayList();
//        sections.add("Trailers");
//        sections.add("Reviews");

//        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
//                getActivity(),
//                sections,
//                android.R.layout.simple_list_item_1,
//                null,
//                null,
//                null,
//                0,
//                null);

//        mTrailersListView.setAdapter(adapter);
//
//        mTrailersListView.
//



        return mRootView;
    }

    // This is called both from single and two panel to populate the data with a common struct
    // bundle
    private void fillForm(Bundle arguments) {

        String title = arguments.getString(MovieEntry.COLUMN_TITLE);
        String originalTitle = arguments.getString(MovieEntry.COLUMN_ORIGINAL_TITLE);
        Date release_date = new Date(Long.parseLong(
                arguments.getString(MovieEntry.COLUMN_RELEASE_DATE)));
        String posterPath = arguments.getString(MovieEntry.COLUMN_POSTER_PATH);
        String voteAverage = arguments.getString(MovieEntry.COLUMN_VOTE_AVERAGE);
        String overView = arguments.getString(MovieEntry.COLUMN_OVERVIEW);
        boolean favorite = (arguments.getString(MovieEntry.COLUMN_FAVORITE).equals("1"));

        // Showing the original title as Movie title
        titleView.setText(originalTitle);

        // extract the year
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        yearView.setText(df.format(release_date));

        // Poster image is composed of 3 parts
        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
        // 2 - Then you will need a ‘size’, which will be one of the following:
        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
//        String poster = "http://image.tmdb.org/t/p/w500"+arguments.getString(MovieEntry.COLUMN_POSTER_PATH);
        String poster = "http://image.tmdb.org/t/p/w185"+ posterPath;
        Log.d(LOG_TAG, "Loading poster url => " + poster);
//        OkHttpClient okHttpClient = HelperOkHttpClient.getOkHttpClientBuilder().build();
//        Picasso picasso = new Picasso.Builder(mRootView.getContext())
//                .downloader(new OkHttp3Downloader(okHttpClient))
//                .build();
        Picasso picasso = HelperOkHttpClient.getPicassoInstance(mRootView.getContext());
        picasso.load(poster)
                .into(posterView);
//        Picasso picasso = Picasso.with(mRootView.getContext());
//        picasso.setIndicatorsEnabled(true);
//        picasso.load(poster).into(posterView);

        // Accessibility feature
        posterView.setContentDescription(arguments.getString(MovieEntry.COLUMN_POSTER_PATH));

        voteAverageView.setText(voteAverage + getString(R.string.vote_avg_string));

        float voteAverageNumber = Float.parseFloat(voteAverage) / 2;
        voteAverageRatingBar.setRating(voteAverageNumber);

        descriptionView.setText(overView);

        starButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                //mUri = content://com.popumovies/movie/4
                onClickFavorite(v, mUri);
            }
        });
        // Select star if favorite

        starButton.setSelected(favorite);

        Button favButton = (Button) mRootView.findViewById(R.id.button_favorite);
        favButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                //mUri = content://com.popumovies/movie/4
                ImageButton starButton = (ImageButton)
                        mRootView.findViewById(R.id.button_star_favorite);
                onClickFavorite(starButton,mUri);
            }
        });

        getLoaderManager().initLoader(FRAGMENT_DETAIL_VIDEO_LOADER, null, this);
        getLoaderManager().initLoader(FRAGMENT_DETAIL_REVIEW_LOADER, null, this);

//
//        mRecyclerViewVideo = (RecyclerView) mRootView.findViewById(R.id.recyclerview_video);
////        mRecyclerViewVideo.setMinimumHeight(300);
////        mRecyclerViewVideo.setHasFixedSize(true);
////        mLayoutManager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
//        mLayoutManager = new VarColumnGridLayoutManager(getActivity(), 200);
//
//        mRecyclerViewVideo.setLayoutManager(mLayoutManager);
////        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);
////        mRecyclerViewVideo.addItemDecoration(itemDecoration);
//        mVideoAdapter = new VideoAdapter(getActivity(), null, 0);
//        mRecyclerViewVideo.setAdapter(mVideoAdapter);
//
//        getLoaderManager().initLoader(FRAGMENT_DETAIL_VIDEO_LOADER, null, this);
//
//
//        mRecyclerViewReview = (RecyclerView) mRootView.findViewById(R.id.recyclerview_review);
//        mRecyclerViewReview.setHasFixedSize(true);
//        mReviewLayoutManager = new LinearLayoutManager(getActivity());
//        mRecyclerViewReview.setLayoutManager(mReviewLayoutManager);
//        mReviewAdapter = new ReviewAdapter(getActivity(), null, 0);
//        mRecyclerViewReview.setAdapter(mReviewAdapter);
//
//        getLoaderManager().initLoader(FRAGMENT_DETAIL_REVIEW_LOADER, null, this);
//
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(LOG_TAG, "Destroying fragment with name " + mTitle);
//    }

    public void onClickFavorite(View v, Uri mUri) {
        v.setSelected(!v.isSelected());
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, v.isSelected());
        getActivity().getContentResolver().update(
                mUri,
                movieValues,
                null,
                null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return true;
    }
    //    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        // Prepare the loader.  Either re-connect with an existing one,
//        // or start a new one.
////        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
//        Intent intent = getActivity().getIntent();
//        if (intent == null || intent.getData() == null) {
//            return null;
//        }
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        if (id == FRAGMENT_DETAIL_VIDEO_LOADER) {
            Uri videoUri = MovieEntry.buildMovieWithVideos(mMovieId);
            //mUri = mUri.buildUpon().appendPath("extras").build();
            if (videoUri != null) {
                return new CursorLoader(
                        getActivity(),
                        videoUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        else if (id == FRAGMENT_DETAIL_REVIEW_LOADER) {
            Uri reviewUri = MovieEntry.buildMovieWithReviews(mMovieId);
            //mUri = mUri.buildUpon().appendPath("extras").build();
            if (reviewUri != null) {
                return new CursorLoader(
                        getActivity(),
                        reviewUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        else if (id == FRAGMENT_DETAIL_MOVIE_LOADER) {
            mUri = MovieEntry.buildSingleMovieUri(mMovieId);
            if (mUri != null) {
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        return null;    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        if (loader.getId() == FRAGMENT_DETAIL_VIDEO_LOADER)
            mVideoAdapter.swapCursor(data);
        else if (loader.getId() == FRAGMENT_DETAIL_REVIEW_LOADER)
            mReviewAdapter.swapCursor(data);
        else if (loader.getId() == FRAGMENT_DETAIL_MOVIE_LOADER) {
            // Instead of using an adapter, we fill a Bundle (args) with cursor's data
            updateMovieDetails(data);
//
//        // Select star if favorite
//        mStarButton.setSelected((data.getInt(MovieContract.MovieEntry.COLUMN_POS_FAVORITE) != 0));
//
//        // Read description from cursor and update view
//        mTitleView.setText(data.getString(MovieContract.MovieEntry.COLUMN_POS_ORIGINAL_TITLE));
//        // get the release date
//        Date release_date = new Date(data.getLong(MovieContract.MovieEntry.COLUMN_POS_RELEASE_DATE));
//        // extract the year
//        SimpleDateFormat df = new SimpleDateFormat("yyyy");
//        mYearView.setText(df.format(release_date));
////        mPopularityView.setText(data.getString(MovieContract.MovieEntry.COLUMN_POS_POPULARITY));
//        String voteAverage = data.getString(MovieContract.MovieEntry.COLUMN_POS_VOTE_AVERAGE);
//        mVoteAverageView.setText(voteAverage + getString(R.string.vote_avg_string));
//
//        float voteAverageNumber = Float.parseFloat(voteAverage) / 2;
//
//        mVoteAverageRatingBar.setRating(voteAverageNumber);
//        mDescriptionView.setText(data.getString(MovieContract.MovieEntry.COLUMN_POS_OVERVIEW));
//
//        // Poster image is composed of 3 parts
//        // 1 - The base URL will look like: http://image.tmdb.org/t/p/.
//        // 2 - Then you will need a ‘size’, which will be one of the following:
//        //  "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
//        // 3 - And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
//        String poster = "http://image.tmdb.org/t/p/w185"+data.getString(MovieContract.MovieEntry.COLUMN_POS_POSTER_PATH);
//        Log.d(LOG_TAG, "Loading poster url => " + poster);
//        Picasso.with(mRootView.getContext()).load(poster)
//                .into(mPosterView);
//        mPosterView.setContentDescription(data.getString(
//                MovieContract.MovieEntry.COLUMN_POS_ORIGINAL_TITLE));
        }
    }

    // When in twopane, this is called after the cursor loader finishes, to
    // map the cursor to the arguments array
    private void updateMovieDetails(Cursor cursor) {
        if (cursor.moveToFirst()) {
            Bundle args = new Bundle();
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                args.putString(cursor.getColumnName(i), cursor.getString(i));
            }
            fillForm(args);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    // When in twoPane, it creates a new loader to grab data. It doesn't use an adapter
    public void onTwoPaneMovieSelected(long id) {
        mMovieId = id;
        getLoaderManager().initLoader(FRAGMENT_DETAIL_MOVIE_LOADER, null, this);

    }

    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

}

