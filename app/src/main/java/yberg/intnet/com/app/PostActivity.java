package yberg.intnet.com.app;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Post post;
    private ArrayList<Post> mPosts;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Post");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        mPosts = new ArrayList<>();
        post = (Post) getIntent().getSerializableExtra("post");
        mPosts.add(post);

        updatePost();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        updatePost();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CardAdapter(this, mPosts, false);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void updatePost() {
        StringRequest getPostRequest = new StringRequest(Request.Method.POST, Database.GET_POST_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("getPost response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        mPosts.clear();
                        JSONObject post = jsonResponse.getJSONObject("post");
                        JSONObject user = post.getJSONObject("user");
                        JSONArray comments = post.getJSONArray("comments");
                        ArrayList<Comment> mComments = new ArrayList<>();
                        for (int i = 0; i < comments.length(); i++) {
                            JSONObject comment = comments.getJSONObject(i);
                            JSONObject usr = comment.getJSONObject("user");
                            mComments.add(new Comment(
                                    comment.getInt("cid"),
                                    new User(
                                            usr.getInt("uid"),
                                            usr.getString("username"),
                                            usr.getString("name"),
                                            usr.getString("image")
                                    ),
                                    comment.getString("text"),
                                    comment.getString("commented")
                            ));
                        }
                        System.out.println("user: " + user.getInt("uid"));
                        mPosts.add(new Post(
                                post.getInt("pid"),
                                new User(
                                        user.getInt("uid"),
                                        user.getString("username"),
                                        user.getString("name"),
                                        user.getString("image")
                                ),
                                post.getString("text"),
                                post.getString("posted"),
                                post.getInt("numberOfComments"),
                                mComments,
                                post.getInt("upvotes"),
                                post.getInt("downvotes"),
                                post.getInt("voted"),
                                post.getString("image")
                        ));
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("pid", "" + post.getPid());
                return parameters;
            }
        };
        requestQueue.add(getPostRequest);
    }

}