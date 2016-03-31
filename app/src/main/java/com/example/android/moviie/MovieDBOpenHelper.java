package com.example.android.moviie;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MovieDBOpenHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for the database
    public static final String MOVIE_TABLE = "movie_table";
    public static final String ID = "_id";
    public static final String MOVIE_ID = "movie_id";
    public static final String BACKDROP_PATH = "backdrop_path";
    public static final String TITLE = "title";
    public static final String OVERVIEW = "overview";
    public static final String RELEASE_DATE = "release_date";
    public static final String POSTER_PATH = "poster_path";
    public static final String POPULARITY = "popularity";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String VOTE_COUNT = "vote_count";

    public static final String[] ALL_COLUMNS =
            {ID, MOVIE_ID, BACKDROP_PATH, TITLE,
                    OVERVIEW, RELEASE_DATE, POSTER_PATH,
                    POPULARITY, VOTE_AVERAGE, VOTE_COUNT};

    // String to create Table
    private  static final String TABLE_CREATE =
            "CREATE TABLE " + MOVIE_TABLE + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MOVIE_ID + " INTEGER, " +
                    BACKDROP_PATH + " INTEGER, " +
                    TITLE + " TEXT, " +
                    OVERVIEW + " TEXT, " +
                    RELEASE_DATE + " TEXT, " +
                    POSTER_PATH + " TEXT, " +
                    POPULARITY + " REAL, " +
                    VOTE_AVERAGE + " INTEGER, " +
                    VOTE_COUNT + " INTEGER" +
                    ")";
    /**
     * Create a helper object to create, open, and/or manage a database.
     * The database is not actually created or opened until one of
     * {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
     * <p/>
     * <p>Accepts input param: a concrete instance of {@link DatabaseErrorHandler} to be
     * used to handle corruption when sqlite reports database corruption.</p>
     *
     * @param context      to use to open or create the database
     *
     */
    public MovieDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TABLE_CREATE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + MOVIE_TABLE);
        onCreate(db);
    }
}
