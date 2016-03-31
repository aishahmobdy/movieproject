package com.example.android.moviie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MovieDetailActivity extends AppCompatActivity {
    public static final String MOVIE_DB_EXTRA = "Movie_DB_Extra";
    public static String MOVIE_EXTRA = "Movies_Extra";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent calledIntent = getIntent();
        Movie movie = (Movie) calledIntent.getSerializableExtra(MOVIE_EXTRA);
        MovieDetailFragment detailFragment = MovieDetailFragment.newInstance(movie);

        getFragmentManager().beginTransaction().replace(R.id.detail_activity_container, detailFragment)
                .commit();
    }
}
