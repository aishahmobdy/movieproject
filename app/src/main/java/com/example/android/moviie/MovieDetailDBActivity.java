package com.example.android.moviie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;

public class MovieDetailDBActivity extends AppCompatActivity {
    public static final String MOVIE_DB_EXTRA = "Movie_DB_Extra";
    public static String MOVIE_EXTRA = "Movies_Extra";
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent calledIntent = getIntent();
        int movie =  calledIntent.getIntExtra(MOVIE_DB_EXTRA, 0);
        MovieDetailDBFragment detailFragment = MovieDetailDBFragment.newInstance(movie);

        getFragmentManager().beginTransaction().replace(R.id.detail_activity_container, detailFragment)
                .commit();
    }


}
