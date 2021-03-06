package com.application.amrudesh.moviedb_udacity.Activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.application.amrudesh.moviedb_udacity.Data.MovieViewModel;
import com.application.amrudesh.moviedb_udacity.Data.YoutubeAdapter;
import com.application.amrudesh.moviedb_udacity.Model.Movie;
import com.application.amrudesh.moviedb_udacity.Model.Youtube;
import com.application.amrudesh.moviedb_udacity.R;
import com.application.amrudesh.moviedb_udacity.Util.Constants;
import com.application.amrudesh.moviedb_udacity.Util.Prefs;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Details extends AppCompatActivity implements Serializable {
    private Movie movie,mvData;
    MainActivity mainActivity;
    TextView mvName,storyPlot,ratingtxt,releaseDate;
    private RecyclerView recyclerView;
    private YoutubeAdapter youtubeAdapter;
    private List<Youtube> youtubeList;

    RatingBar ratingBar;
    ImageView imageView;
    String videos;
    private RequestQueue queue;
    private String movieId,movieName,plot,rating,release,img;
    LikeButton likeButton;
    private MovieViewModel movieViewModel;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Button btn = (Button) findViewById(R.id.review_btn);
        mvData = new Movie();
        movie =(Movie) getIntent().getSerializableExtra("movie");
        movieId = movie.getMovieID();
        url = Constants.search_base_id_left+ movieId + Constants.getSearch_base_id_right+"&append_to_response=videos";
        movieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
        queue =Volley.newRequestQueue(this);
        setUI();
        getDetails(url);
        setBtnStatus();
        Log.i("Movie",movie.getFavBtn().toString());

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(),Review.class);
                intent.putExtra("movie",movie);
                startActivity(intent);
            }
        });
        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                mvData.setFavBtn(true);
                addToDatabase(movieId,movieName,plot,rating,release,true);
                Toast.makeText(Details.this, "Added To Favorites", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void unLiked(LikeButton likeButton) {
                deleteFromDatabase();
                mvData.setFavBtn(false);
                Toast.makeText(Details.this, "Removed From Favorites", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void setUI() {
        mvName = (TextView) findViewById(R.id.mov_name);
        storyPlot=(TextView)findViewById(R.id.story_line);
        ratingBar =(RatingBar) findViewById(R.id.rating);
        imageView= (ImageView)findViewById(R.id.mov_img);
        likeButton =(LikeButton) findViewById(R.id.heart_button);
        ratingtxt =(TextView)findViewById(R.id.ratingtxt);
        releaseDate =(TextView) findViewById(R.id.releasedDate);
        youtubeList = new ArrayList<>();
        youtubeAdapter = new YoutubeAdapter(this,youtubeList);
        recyclerView = (RecyclerView) findViewById(R.id.youtube_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(youtubeAdapter);


    }

    private void getDetails(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    movieName =response.getString("original_title");
                    plot =response.getString("overview");
                    release =response.getString("release_date");
                    rating=response.getString("vote_average");
                    img=response.getString("poster_path");



                    releaseDate.setText("Released Date:\t" + response.getString("release_date"));
                    ratingtxt.setText("Average Rating:\t" + response.getString("vote_average"));
                    mvName.setText(response.getString("original_title"));
                    storyPlot.setText(response.getString("overview"));
                    Picasso.get()
                            .load(Constants.image_url+response.getString("poster_path"))
                            .into(imageView);
                    ratingBar.setRating(Float.valueOf(response.getString("vote_average")));
                    videos=response.getString("videos");

                    JSONObject jsonObject = new JSONObject(videos);
                    String videoLnk = jsonObject.getString("results");
                    JSONArray jsonArray = new JSONArray(videoLnk);
                    for (int i=0; i < jsonArray.length();i++)
                    {
                        Youtube youtube = new Youtube();
                        JSONObject parObj = jsonArray.getJSONObject(i);
                        youtube.setLinks(parObj.getString("key"));
                        youtube.setNames(parObj.getString("name"));
                        youtubeList.add(youtube);
                        Log.i("Video",parObj.getString("key"));

                    }

                    youtubeAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Log.i("Movie",response.toString());


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Movie",error.toString());
            }
        });
        queue.add(jsonObjectRequest);

    }




    public void addToDatabase(String mvID,String mvNm,String plt,String rt,String rel,boolean btn)
    {

        Movie movie = new Movie(mvID,mvNm,plt,img,rt,rel,btn);
        movieViewModel.insert(movie);
    }
    public void deleteFromDatabase()
    {

        movieViewModel.delete(movie);
    }
    public void setBtnStatus()
    {
        if (movie.getFavBtn())
        {
            likeButton.setLiked(true);
        }
        else
        {
            likeButton.setLiked(false);
        }
    }
}
