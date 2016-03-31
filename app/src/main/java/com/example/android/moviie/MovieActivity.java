package com.example.android.moviie;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.FragmentManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MovieActivity extends AppCompatActivity implements MovieActivityFragment.MovieGridListener,
        MovieDBLIstFragment.DBGridClickListener{
    public static final String MOVIE_DB_EXTRA = "Movie_DB_Extra";
    public static String MOVIE_EXTRA = "Movies_Extra";
    private static final String DETAIL_FRAG = "detail_frag";
    FragmentManager fragmentManager;
    MovieActivityFragment movieActivityFragment;
    ArrayList<Movie> mPopularList;
    ArrayList<Movie> mTopVotedList;
    final static String POP_LIST = "popList";
    final static String TOP_VOTE_LIST = "topVoteList";
    final static String TAG = "MovieActivity";

    // Handling Saving Data before App is closed
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putSerializable(POP_LIST, mPopularList);
        outState.putSerializable(TOP_VOTE_LIST, mTopVotedList);
        super.onSaveInstanceState(outState);

    }

    // Loading SavedInstance
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


        // Loading Saved data
        mPopularList =(ArrayList<Movie>) savedInstanceState.getSerializable(POP_LIST);
        mTopVotedList =(ArrayList<Movie>) savedInstanceState.getSerializable(TOP_VOTE_LIST);
        //loadPreferenceList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPopularList == null || mTopVotedList == null) { // Checking to see if data is present before loading
            if (NetworkConnections.networkcheck(MovieActivity.this)) {
                new MainSync().execute();
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getString(R.string.network_alert_title));
                dialog.setMessage(getString(R.string.network_alert_message));
                dialog.setCancelable(false);
                dialog.show();
            }
        } else {
            loadPreferenceList();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load Framments
        loadFragmentManagerCheck();


    }


    @Override
    public void onMovieSelected(Movie movie) {

        Toast.makeText(this, "You selected " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movie);
        if (findViewById(R.id.fragmentDetailContainer) != null) {
            fragmentManager.beginTransaction().replace(R.id.fragmentDetailContainer, detailFragment, DETAIL_FRAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MOVIE_EXTRA, movie);
            startActivity(intent);
        }


    }

    @Override
    public void onSettingMenuClicked() {
        Intent optionIntent = new Intent(this, SettingActivity.class);
        startActivity(optionIntent);
    }

    @Override
    public void onDBGridItemClicked(int Movie_ID) {


        MovieDetailDBFragment detailFragment = MovieDetailDBFragment.newInstance(Movie_ID);
        if (findViewById(R.id.fragmentDetailContainer) != null) {
            fragmentManager.beginTransaction().replace(R.id.fragmentDetailContainer, detailFragment, DETAIL_FRAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailDBActivity.class);
            intent.putExtra(MOVIE_DB_EXTRA, Movie_ID);
            startActivity(intent);
        }

    }

    public class MainSync extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MovieActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            dialog.setTitle("Loading...");
            dialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {


            String WebAddress;
            String WebAddressVote;


            WebAddress = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=b76fbdb4000cbe9802f867f207f16895"
                    ;

            WebAddressVote = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&api_key=b76fbdb4000cbe9802f867f207f16895"
                    ;

            mPopularList = new ArrayList<>();
            mTopVotedList = new ArrayList<>();


            URLResult(WebAddress, mPopularList, WebAddressVote, mTopVotedList);







        return null;
    }


    @Override
    protected void onPostExecute(Void s) {
        super.onPostExecute(s);
        loadPreferenceList();

        dialog.cancel();

    }

}

    private void loadPreferenceList() {
        // Checking Shared Preference for data
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(MovieActivity.this);
        if (sharedPreferences.getString("ORG_PREF_LIST", "popular").equals("popular")) {
            loadFragment(mPopularList);
        } else if (sharedPreferences.getString("ORG_PREF_LIST", "popular").equals("rated")){
            loadFragment(mTopVotedList);

        }else {
            fragmentManager = getFragmentManager();
            loadFragmentManagerCheck();
            MovieDBLIstFragment movieDBLIstFragment = new MovieDBLIstFragment();
            fragmentManager.beginTransaction().replace(R.id.fragmentGridListContainer, movieDBLIstFragment).commit();
        }
    }

    private void loadFragment(ArrayList<Movie> mPopularList) {

        // Load Fragments
        movieActivityFragment = MovieActivityFragment.instanceOf(mPopularList);
        loadFragmentManagerCheck();


        fragmentManager.beginTransaction().replace(R.id.fragmentGridListContainer, movieActivityFragment).commit();
    }

    private void loadFragmentManagerCheck() {
        // Load Framments
        fragmentManager = getFragmentManager();

        if (findViewById(R.id.detail_activity_container)==null && fragmentManager.findFragmentByTag(DETAIL_FRAG) != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(DETAIL_FRAG));
        }
    }

    private void URLResult(String webAddress, ArrayList<Movie> _List, String webAdressVote, ArrayList<Movie> _voteList ) {
        try {



            URL url = new URL(webAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            String results = IOUtils.toString(inputStream);
            jsonParser(results, _List);




            URL voteUrl = new URL(webAdressVote);
            HttpURLConnection connection1 = (HttpURLConnection) voteUrl.openConnection();
            InputStream inputStream1 = connection1.getInputStream();
            String voteResuts = IOUtils.toString(inputStream1);
            jsonParser(voteResuts, _voteList);

            inputStream.close();
            inputStream1.close();


        } catch (IOException e) {
            e.printStackTrace();


        }
    }

    private void jsonParser(String s, ArrayList<Movie> movies) {
        try {
            JSONObject mainObject = new JSONObject(s);

            JSONArray resultsArray = mainObject.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject indexObject = resultsArray.getJSONObject(i);
                Movie indexMovie = new Movie();
                indexMovie.setBackdrop_path(indexObject.getString("backdrop_path"));
                indexMovie.setId(indexObject.getInt("id"));
                indexMovie.setOriginal_title(indexObject.getString("original_title"));
                indexMovie.setOverview(indexObject.getString("overview"));
                indexMovie.setRelease_date(indexObject.getString("release_date"));
                indexMovie.setPoster_path(indexObject.getString("poster_path"));
                indexMovie.setPopularity(indexObject.getDouble("popularity"));
                indexMovie.setTitle(indexObject.getString("title"));
                indexMovie.setVote_average(indexObject.getString("vote_average"));
                indexMovie.setVote_count(indexObject.getInt("vote_count"));



                movies.add(indexMovie); // Add each item to the list

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON Error", e);
        }
    }




}
