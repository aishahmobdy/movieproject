package com.example.android.moviie;

import android.app.Activity;
import android.content.Context;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import  com.example.android.moviie.Movie;
import com.example.android.moviie.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieActivityFragment extends Fragment implements AdapterView.OnItemClickListener {

    final static String TAG = "MovieActivityFragment";
    private static final String MOVIE_LIST = "Movie_List";

    GridView mMainGrid;
    ArrayList<Movie> mMovieList;


    public static MovieActivityFragment instanceOf(ArrayList<Movie> _movieList){
        MovieActivityFragment fragment = new MovieActivityFragment();

        Bundle arg = new Bundle();
        arg.putSerializable(MOVIE_LIST, _movieList);
        fragment.setArguments(arg);

        return fragment;

    }



    public MovieActivityFragment() {
    }

    private MovieGridListener listener;

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
        listener.onMovieSelected((Movie) parent.getAdapter().getItem(position));
    }

    public interface MovieGridListener{
        void onMovieSelected(Movie movie);
        void onSettingMenuClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof  MovieGridListener){
            listener = (MovieGridListener) activity;
        } else {
            throw new IllegalArgumentException("The fragment listener is not connected to the activity");
        }
    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_movie, container, false);

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_movie, menu);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            listener.onSettingMenuClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        Log.i(TAG, "onActivityCreated called");

        mMainGrid = (GridView) getActivity().findViewById(R.id.topMovieGrid);
        if (getArguments()!= null) {
            mMovieList = (ArrayList<Movie>) getArguments().getSerializable(MOVIE_LIST);
            CustomGridAdapter adapter = new CustomGridAdapter(getActivity(), mMovieList);
            mMainGrid.setAdapter(adapter);
            mMainGrid.setOnItemClickListener(this);
        }





    }







    public class CustomGridAdapter extends BaseAdapter {
        Context context;
        ArrayList<Movie> movieList;

        public CustomGridAdapter(Context context, ArrayList<Movie> movieDbList) {
            this.context = context;
            this.movieList = movieDbList;
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return movieList.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Movie getItem(int position) {
            return movieList.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 123456000 + position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.custom_item_row, parent, false);
            }
            Movie movieDb = getItem(position);


            ImageView imageViewcustom = (ImageView) convertView.findViewById(R.id.customImageView);
            Picasso.with(context).load("https://image.tmdb.org/t/p/w185" + movieDb.getPoster_path())
                    .placeholder(R.drawable.poster_place_holder)
                    .into(imageViewcustom);

            return convertView;
        }
    }
}
