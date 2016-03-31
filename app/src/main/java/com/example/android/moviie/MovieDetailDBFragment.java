package com.example.android.moviie;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

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


public class MovieDetailDBFragment extends Fragment implements AdapterView.OnItemClickListener{
    public static final String MOVIE_DB_EXTRA = "Movie_DB_Extra";
    public static String MOVIE_EXTRA = "Movies_Extra";

    private static final String TAG = "MovieDetailDBFragment";
    // Global (Member) Cursor Variable
    Cursor mCursor;
    private ArrayList<Trailer> trailerArrayList;
    private ArrayList<Review> reviewArrayList;

    private ShareActionProvider mShareActionProvider;

    public static MovieDetailDBFragment newInstance(int movie_id) {
        MovieDetailDBFragment fragment = new MovieDetailDBFragment();

        Bundle arg = new Bundle();
        arg.putInt(MOVIE_DB_EXTRA, movie_id);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle loadArgs = getArguments();


        if (loadArgs != null && getActivity().findViewById(R.id.detail_image)!=null){
            int movieIDRetreived = loadArgs.getInt(MOVIE_DB_EXTRA);
            Uri uri = Uri.parse(MovieProvider.CONTENT_URI + "/" + movieIDRetreived);
            String movieFilter = MovieDBOpenHelper.MOVIE_ID + "=" + uri.getLastPathSegment();
            Log.d(TAG, "Uri: " + uri.toString());

            mCursor = getActivity().getContentResolver().query(uri, MovieDBOpenHelper.ALL_COLUMNS,
                    movieFilter, null, null, null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();

                ((TextView) getActivity().findViewById(R.id.detail_title)).setText(mCursor.
                        getString(mCursor.getColumnIndex(MovieDBOpenHelper.TITLE)));
                Picasso.with(getActivity()).load("https://image.tmdb.org/t/p/w185" + mCursor.
                        getString(mCursor.getColumnIndex(MovieDBOpenHelper.POSTER_PATH)))
                        .placeholder(R.drawable.poster_place_holder)
                        .into((ImageView) getActivity().findViewById(R.id.detail_image));
                ((TextView) getActivity().findViewById(R.id.detail_release_date)).setText(mCursor.
                        getString(mCursor.getColumnIndex(MovieDBOpenHelper.RELEASE_DATE)));
                ((TextView) getActivity().findViewById(R.id.detail_popularity)).setText("Popularity\n" + mCursor.
                        getDouble(mCursor.getColumnIndex(MovieDBOpenHelper.POPULARITY)));
                ((TextView) getActivity().findViewById(R.id.detail_vote_count)).setText("Vote Count: " + mCursor.
                        getInt(mCursor.getColumnIndex(MovieDBOpenHelper.VOTE_COUNT)));
                ((TextView) getActivity().findViewById(R.id.detail_vote_average)).setText("vote Average: " + mCursor.
                        getString(mCursor.getColumnIndex(MovieDBOpenHelper.VOTE_AVERAGE)));
                ((TextView) getActivity().findViewById(R.id.detail_overview)).setText(mCursor.
                        getString(mCursor.getColumnIndex(MovieDBOpenHelper.OVERVIEW)));
                getActivity().findViewById(R.id.favorite_button).setVisibility(View.GONE);

                new reviewAndTrailerSync().execute(mCursor.getInt(mCursor.getColumnIndex(MovieDBOpenHelper.MOVIE_ID)));

            }


        }
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
                        Log.i("trailer = ", trailer.getId()+"");
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
            listView.setOnItemClickListener(MovieDetailDBFragment.this);
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
