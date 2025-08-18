package com.example.seekshakcom.tutor

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seekshakcom.R
import com.example.seekshakcom.adapter.MyHomeTuitionsAdapter
import com.example.seekshakcom.model.TuitionPost
import com.example.seekshakcom.network.RetrofitInstance
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllTuitionPostsActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var recyclerTuitionPosts: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var backArrow: ImageView

    private var tuitionPosts: ArrayList<TuitionPost>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_tuition_posts)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        swipeRefresh = findViewById(R.id.swipeRefresh)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        recyclerTuitionPosts = findViewById(R.id.recyclerTuitionPosts)
        emptyText = findViewById(R.id.emptyText)
        backArrow = findViewById(R.id.backArrow)

        recyclerTuitionPosts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerTuitionPosts.itemAnimator = DefaultItemAnimator()

        // ✅ Check if posts were passed via Intent
        tuitionPosts = intent.getParcelableArrayListExtra("ALL_TUITION_POSTS")
        val passedPosts = intent.getParcelableArrayListExtra<TuitionPost>("all_tuitions")

        when {
            !tuitionPosts.isNullOrEmpty() -> setupRecyclerView(tuitionPosts!!)
            !passedPosts.isNullOrEmpty() -> setupRecyclerView(passedPosts, showViewAll = false)
            else -> loadTuitionPosts()
        }

        swipeRefresh.setOnRefreshListener {
            loadTuitionPosts()
        }

        backArrow.setOnClickListener { finish() }
    }

    /** 🔄 Common sorting + adapter setup */
    private fun setupRecyclerView(posts: List<TuitionPost>, showViewAll: Boolean = false) {
        val sortedPosts = sortPosts(posts)

        val tuitionAdapter = MyHomeTuitionsAdapter(
            items = sortedPosts,
            showViewAll = showViewAll,
            onApplyClick = { post ->
                Toast.makeText(this, "Apply clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show()
            },
            onFavouriteClick = { post ->
                Toast.makeText(this, "Favourite clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerTuitionPosts.adapter = tuitionAdapter
        recyclerTuitionPosts.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    /** 🔄 Sort active first */
    private fun sortPosts(posts: List<TuitionPost>): List<TuitionPost> {
        val activePosts = posts.filter { it.status == "active" }
        val inactivePosts = posts.filter { it.status != "active" }
        return activePosts + inactivePosts
    }

    /** 🔄 API call */
    private fun loadTuitionPosts() {
        swipeRefresh.isRefreshing = true
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.GONE

        val prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()

        if (lat == null || lon == null) {
            showError("Location not set")
            return
        }

        RetrofitInstance.instance.getNearbyPosts(lat, lon)
            .enqueue(object : Callback<List<TuitionPost>> {
                override fun onResponse(
                    call: Call<List<TuitionPost>>,
                    response: Response<List<TuitionPost>>
                ) {
                    swipeRefresh.isRefreshing = false
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE

                    val tuitionPosts = response.body()
                    if (response.isSuccessful && !tuitionPosts.isNullOrEmpty()) {
                        setupRecyclerView(tuitionPosts)
                    } else {
                        showError("Tuitions are not available near you.\nPull to refresh.")
                    }
                }

                override fun onFailure(call: Call<List<TuitionPost>>, t: Throwable) {
                    swipeRefresh.isRefreshing = false
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    showError("Failed to load tuitions. Try again.\nPull to refresh.")
                }
            })
    }

    /** 🔄 Show error message */
    private fun showError(message: String) {
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}
