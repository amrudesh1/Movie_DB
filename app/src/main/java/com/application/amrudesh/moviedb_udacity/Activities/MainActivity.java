package com.application.amrudesh.moviedb_udacity.Activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.amrudesh.moviedb_udacity.Data.MovieAdapter;
import com.application.amrudesh.moviedb_udacity.Data.MovieViewModel;
import com.application.amrudesh.moviedb_udacity.Model.Movie;
import com.application.amrudesh.moviedb_udacity.R;
import com.application.amrudesh.moviedb_udacity.Util.Constants;
import com.application.amrudesh.moviedb_udacity.Util.Prefs;
import com.claudiodegio.msv.MaterialSearchView;
import com.claudiodegio.msv.OnSearchViewListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class MainActivity extends AppCompatActivity implements OnSearchViewListener {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    MovieViewModel movieViewModel;
    private RequestQueue requestQueue;
    private MaterialSearchView searchView;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    RadioButton rd1,rd2,rd3,rd4;
    Boolean a,b,c,d;
    String search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AlertDialog alertDialog;
        searchView = (MaterialSearchView) findViewById(R.id.sv);
        searchView.setOnSearchViewListener(MainActivity.this);
        requestQueue = Volley.newRequestQueue(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));


         Prefs prefs = new Prefs(MainActivity.this);
         search = prefs.getSearch();

         if (savedInstanceState == null)
         {
             movieList = getMovies(search);
         }
        movieList = new ArrayList<>();
        //getMovies(search);
       //
        movieAdapter = new MovieAdapter(this, movieList);
        recyclerView.setAdapter(movieAdapter);
    }





    public List<Movie> getMovies(String searchTerm) {

        a = true;
        c = d = false;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.COMPLETE_URL + searchTerm, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray movieArray = response.getJSONArray("results");
                    for (int i = 0; i < movieArray.length(); i++) {
                        JSONObject movieObj = movieArray.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setTitle(movieObj.getString("title"));
                        movie.setReleaseDate(movieObj.getString("release_date"));
                        movie.setImage(movieObj.getString("poster_path"));
                        movie.setPlot(movieObj.getString("overview"));
                        movie.setRating(movieObj.getString("vote_average"));
                        movie.setMovieID(movieObj.getString("id"));
                        movie.setFavBtn(false);


                        movieList.add(movie);
                        //TODO:Remove The LOG from Program

                        Log.i("Movie", movie.getTitle());
                        Log.i("Movie", "Year Released:"+ movie.getReleaseDate());
                        Log.i("Movie", movie.getImage());
                        Log.i("Movie", movie.getPlot());
                        Log.i("Movie", movie.getRating());
                        Log.i("Movie",movie.getMovieID());



                    }
                    movieAdapter.notifyDataSetChanged();// This is very Important
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("Movie",error.toString());

            }
        });
        requestQueue.add(jsonObjectRequest);
        return movieList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }


    @Override
    public void onSearchViewShown() {

    }

    @Override
    public void onSearchViewClosed() {

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (!s.isEmpty()) {
            Prefs prefs = new Prefs(MainActivity.this);
            prefs.setSearch(s);
            movieList.clear();
            getMovies(s);
            c = d = false;


        }
        return true;
    }

    @Override
    public void onQueryTextChange(String s) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter)
        {
            showSortDialog();
            return  true;
        }
        else if (id == R.id.fav)
        {
            startActivity(new Intent(this,fav.class));
        }
        return super.onOptionsItemSelected(item);
    }
    public void showSortDialog()
    {
        alertDialogBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.sortdialog,null);

        rd1=(RadioButton) view.findViewById(R.id.nameSort_Btn);
        rd2=(RadioButton) view.findViewById(R.id.release_sort_btn);
        rd3=(RadioButton) view.findViewById(R.id.popularBtn);
        rd4=(RadioButton) view.findViewById(R.id.topRatedBtn);
        Button btn = (Button) view.findViewById(R.id.sort_btn);
        alertDialogBuilder.setView(view);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        final Movie movie = new Movie();

        //Better to Use Switch case rather than using if else condition
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rd2.isChecked())
                {
                    Collections.sort(movieList, new Comparator<Movie>() {
                        @Override
                        public int compare(Movie o1, Movie o2) {
                            return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                        }
                    });
                    movieAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();


                }
                else if(rd1.isChecked())
                {
                    Collections.sort(movieList, new Comparator<Movie>() {
                        @Override
                        public int compare(Movie o1, Movie o2) {
                            return o1.getTitle().compareTo(o2.getTitle());
                        }
                    });
                    movieAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();

                }
                else if (rd3.isChecked())
                {
                    movieList = showPopularMovies();
                    c = true;
                    d= a = false;
                    alertDialog.dismiss();
                }
                else if (rd4.isChecked())
                {
                    movieList =showTopRatedMovies();
                    d = true;
                    c = a = false;
                    alertDialog.dismiss();
                }


            }
        });


    }


    private List<Movie> showPopularMovies() {
        movieList.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.popular_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray movieArray = response.getJSONArray("results");
                    for (int i = 0; i < movieArray.length(); i++) {
                        JSONObject movieObj = movieArray.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setTitle(movieObj.getString("title"));
                        movie.setReleaseDate(movieObj.getString("release_date"));
                        movie.setImage(movieObj.getString("poster_path"));
                        movie.setPlot(movieObj.getString("overview"));
                        movie.setRating(movieObj.getString("vote_average"));
                        movie.setMovieID(movieObj.getString("id"));
                        movie.setFavBtn(false);

                        movieList.add(movie);
                        //TODO:Remove The LOG from Program

                        Log.i("Movie", movie.getTitle());
                        Log.i("Movie", "Year Released:"+ movie.getReleaseDate());
                        Log.i("Movie", movie.getImage());
                        Log.i("Movie", movie.getPlot());
                        Log.i("Movie", movie.getRating());
                        Log.i("Movie",movie.getMovieID());



                    }
                    movieAdapter.notifyDataSetChanged();// This is very Important
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("Movie",error.toString());

            }
        });
        requestQueue.add(jsonObjectRequest);
        return movieList;
    }

    private List<Movie>showTopRatedMovies() {
        movieList.clear();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.top_rated_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray movieArray = response.getJSONArray("results");
                    for (int i = 0; i < movieArray.length(); i++) {
                        JSONObject movieObj = movieArray.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setTitle(movieObj.getString("title"));
                        movie.setReleaseDate(movieObj.getString("release_date"));
                        movie.setImage(movieObj.getString("poster_path"));
                        movie.setPlot(movieObj.getString("overview"));
                        movie.setRating(movieObj.getString("vote_average"));
                        movie.setMovieID(movieObj.getString("id"));
                        movie.setFavBtn(false);

                        movieList.add(movie);
                        //TODO:Remove The LOG from Program

                        Log.i("Movie", movie.getTitle());
                        Log.i("Movie", "Year Released:"+ movie.getReleaseDate());
                        Log.i("Movie", movie.getImage());
                        Log.i("Movie", movie.getPlot());
                        Log.i("Movie", movie.getRating());
                        Log.i("Movie",movie.getMovieID());




                    }
                    movieAdapter.notifyDataSetChanged();// This is very Important
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("Movie",error.toString());

            }
        });
        requestQueue.add(jsonObjectRequest);
        return movieList;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("rd1",a);
        outState.putBoolean("rd3",c);
        outState.putBoolean("rd4",d);



    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

         c = savedInstanceState.getBoolean("rd3");
         d = savedInstanceState.getBoolean("rd4");
         a = savedInstanceState.getBoolean("rd1");

        Log.i("TAG1R",String.valueOf(a));
        Log.i("TAG2R",String.valueOf(c));
        Log.i("TAG3R",String.valueOf(d));


        if(c)
         {
             movieList.clear();
             movieList =showPopularMovies();
         }
         if (d)
         {
             movieList.clear();
             movieList= showTopRatedMovies();
         }
         if(a)
         {
             movieList.clear();
             movieList = getMovies(search);

         }



    }
}

