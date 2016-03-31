package com.example.android.moviie;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.example.android.moviie.Movie;
import com.example.android.moviie.R;
import com.example.android.moviie.MovieDBOpenHelper;
import com.example.android.moviie.MovieProvider;
import com.example.android.moviie.Review;
import com.example.android.moviie.Trailer;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailFragment extends Fragment implements AdapterView.OnItemClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MOVIE_ARG_PARAM = "param1";
    private static final String REVIEW_ARRAY_LIST = "review_array_list";
    private static final String TRAILER_ARRAY_LIST = "trailer_array_list";
    private static final String TAG = "MovieDetailFragment";


    private Movie mMovie;


    ArrayList<Review> reviewArrayList;
    ArrayList<Trailer> trailerArrayList;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param _movie This is the movie passed from the List
     * @return A new instance of fragment MovieDetailFragment.
     */
    public static MovieDetailFragment newInstance(Movie _movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(MOVIE_ARG_PARAM, _movie);
        fragment.setArguments(args);
        return fragment;
    }

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(MOVIE_ARG_PARAM);

        } else if (savedInstanceState.getSerializable(MOVIE_ARG_PARAM) != null) {
            mMovie = (Movie) savedInstanceState.getSerializable(MOVIE_ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onResume() {
        super.onResume();
        if (mMovie != null && getActivity().findViewById(R.id.detail_image) != null) {
            ((TextView) getActivity().findViewById(R.id.detail_title)).setText(mMovie.getTitle());
            Picasso.with(getActivity()).load("https://image.tmdb.org/t/p/w185" + mMovie.getPoster_path())
                    .placeholder(R.drawable.poster_place_holder)
                    .into((ImageView) getActivity().findViewById(R.id.detail_image));
            ((TextView) getActivity().findViewById(R.id.detail_release_date)).setText(mMovie.getRelease_date());
            ((TextView) getActivity().findViewById(R.id.detail_popularity)).setText("Popularity\n" + mMovie.getPopularity());
            ((TextView) getActivity().findViewById(R.id.detail_vote_count)).setText("Vote Count: " + mMovie.getVote_count());
            ((TextView) getActivity().findViewById(R.id.detail_vote_average)).setText("vote Average: " + mMovie.getVote_average()+"/10");
            ((TextView) getActivity().findViewById(R.id.detail_overview)).setText(mMovie.getOverview());

            // Seing if Movie has already been saved
            Uri uri = Uri.parse(MovieProvider.CONTENT_URI + "/" + mMovie.getId());
            String movieFilter = MovieDBOpenHelper.MOVIE_ID + "=" + uri.getLastPathSegment();


            Cursor cursor = getActivity().getContentResolver().query(uri,
                    MovieDBOpenHelper.ALL_COLUMNS, movieFilter, null, null, null);


            if (cursor.getCount() == 0) {
                getActivity().findViewById(R.id.favorite_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Would you like to add " + mMovie.getTitle() + " to your Favorites");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues values = new ContentValues();
                                values.put(MovieDBOpenHelper.BACKDROP_PATH, mMovie.getBackdrop_path());
                                values.put(MovieDBOpenHelper.MOVIE_ID, mMovie.getId());
                                values.put(MovieDBOpenHelper.TITLE, mMovie.getTitle());
                                values.put(MovieDBOpenHelper.POSTER_PATH, mMovie.getPoster_path());
                                values.put(MovieDBOpenHelper.RELEASE_DATE, mMovie.getRelease_date());
                                values.put(MovieDBOpenHelper.POPULARITY, mMovie.getPopularity());
                                values.put(MovieDBOpenHelper.VOTE_COUNT, mMovie.getVote_count());
                                values.put(MovieDBOpenHelper.VOTE_AVERAGE, mMovie.getVote_average());
                                getActivity().getContentResolver().insert(MovieProvider.CONTENT_URI, values);
                                getActivity().findViewById(R.id.favorite_button).setVisibility(View.GONE);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.show();


                    }
                });
            } else {
                getActivity().findViewById(R.id.favorite_button).setVisibility(View.GONE);
            }
            if (reviewArrayList == null || trailerArrayList == null) {
                new reviewAndTrailerSync().execute(mMovie.getId());
            } else {
                setAdapters();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {


        outState.putSerializable(MOVIE_ARG_PARAM, mMovie);
        outState.putSerializable(REVIEW_ARRAY_LIST, reviewArrayList);
        outState.putSerializable(TRAILER_ARRAY_LIST, trailerArrayList);
        super.onSaveInstanceState(outState);
    }


    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Trailer clickedTrailer = (Trailer) parent.getAdapter().getItem(position);
        intent.setData(Uri.parse("https://www.youtube.com/watch?v=" + clickedTrailer.getKey()));
        Log.d("MovieActivty-", "https://www.youtube.com/watch?v=" + clickedTrailer.getKey());
        startActivity(intent);

    }

    public class reviewAndTrailerSync extends AsyncTask<Integer, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            dialog.setTitle("Loading...");
            dialog.show();

        }

        @Override
        protected Void doInBackground(Integer... params) {

            try {
                // Add Trailer
                JSONArray trailerArray = new JSONObject(getURlString("Trailer", params[0]))
                        .getJSONArray("results");
                trailerArrayList = new ArrayList<>();
                if (trailerArray.length() > 0) {
                    for (int n = 0; n < trailerArray.length(); n++) {
                        JSONObject trailerObject = trailerArray.getJSONObject(n);
                        Trailer trailer = new Trailer();
                        trailer.setId(trailerObject.getString("id"));
                        trailer.setKey(trailerObject.getString("key"));
                        trailer.setName(trailerObject.getString("name"));
                        trailer.setSite(trailerObject.getString("site"));
                        trailer.setType(trailerObject.getString("type"));
                        trailerArrayList.add(trailer);

                    }

                }

                // Add Review
                reviewArrayList = new ArrayList<>();
                JSONArray reviewArray = new JSONObject(getURlString("Review", params[0]))
                        .getJSONArray("results");
                if (reviewArray.length() > 0) {
                    for (int x = 0; x < reviewArray.length(); x++) {
                        JSONObject reviewObject = reviewArray.getJSONObject(x);
                        Review review = new Review();
                        review.setId(reviewObject.getString("id"));
                        review.setAuthor(reviewObject.getString("author"));
                        review.setContent(reviewObject.getString("content"));
                        review.setUrl(reviewObject.getString("url"));
                        reviewArrayList.add(review);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            setAdapters();
            dialog.cancel();

        }
    }

    private void setAdapters() {
        if (trailerArrayList.size() > 0) {
            getActivity().findViewById(R.id.trailer_status).setVisibility(View.GONE);
            ListView listView = (ListView) getActivity().findViewById(R.id.detail_trailer_list);
            ArrayAdapter<Trailer> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, trailerArrayList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(MovieDetailFragment.this);
        }

        if (reviewArrayList.size() > 0) {
            getActivity().findViewById(R.id.review_status).setVisibility(View.GONE);
            ListView reviewListView = (ListView) getActivity().findViewById(R.id.detail_review_list);
            ArrayAdapter<Review> reviewArrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, reviewArrayList);
            reviewListView.setAdapter(reviewArrayAdapter);

        }
    }

    /**
     * @param type
     * @param id
     * @return
     */
    private String getURlString(String type, int id) {
        String result;
        String Webaddress;
        if (type.equals("Review")) {
            Webaddress = "http://api.themoviedb.org/3/movie/" + id + "/reviews?sort_by=popularity.desc&api_key=b76fbdb4000cbe9802f867f207f16895"
                    ;
        } else {
            Webaddress = "http://api.themoviedb.org/3/movie/" + id + "/videos?sort_by=popularity.desc&api_key=b76fbdb4000cbe9802f867f207f16895"
                    ;
        }

        try {
            URL url = new URL(Webaddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            result = IOUtils.toString(is);
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = "";
        } catch (IOException e) {
            e.printStackTrace();
            result = "";
        }


        return result;
    }
}
