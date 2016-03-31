package com.example.android.moviie;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class MovieProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.android.moviie.movieprovider";
    private static final String BASE_PATH = "movies";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // Identify the requested operation
    private static final int MOVIES = 1;
    private static final int MOVIES_ID = 2;


    private  static final UriMatcher uriMather = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "note";

    static {
        uriMather.addURI(AUTHORITY, BASE_PATH, MOVIES);
        uriMather.addURI(AUTHORITY, BASE_PATH + "/#", MOVIES_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        MovieDBOpenHelper openHelper = new MovieDBOpenHelper(getContext());
        database = openHelper.getWritableDatabase();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (uriMather.match(uri) == MOVIES_ID){
            selection = MovieDBOpenHelper.MOVIE_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(MovieDBOpenHelper.MOVIE_TABLE, MovieDBOpenHelper.ALL_COLUMNS,
                selection, null,null, null,
                MovieDBOpenHelper.TITLE + " DESC");

    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long id = database.insert(MovieDBOpenHelper.MOVIE_TABLE, null, values);
        return  Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        return database.delete(MovieDBOpenHelper.MOVIE_TABLE, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(MovieDBOpenHelper.MOVIE_TABLE, values, selection, selectionArgs);
    }
}
