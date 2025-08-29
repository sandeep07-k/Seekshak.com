package com.example.seekshakcom.tutor

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
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
import java.text.SimpleDateFormat
import java.util.Locale



class AllTuitionPostsActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var recyclerTuitionPosts: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var backArrow: ImageView
    private lateinit var spinnerSort: Spinner

    private var tuitionPosts: ArrayList<TuitionPost> = arrayListOf()
    private lateinit var tuitionAdapter: MyHomeTuitionsAdapter
    private lateinit var filterSortRow: LinearLayout


    private var isLoading = false
    private var currentPage = 1
    private val pageSize = 20


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_tuition_posts)

        // Status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        swipeRefresh = findViewById(R.id.swipeRefresh)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        recyclerTuitionPosts = findViewById(R.id.recyclerTuitionPosts)
        emptyText = findViewById(R.id.emptyText)
        backArrow = findViewById(R.id.backArrow)
        spinnerSort = findViewById(R.id.spinnerSort)
        filterSortRow = findViewById(R.id.filterSortRow)

        recyclerTuitionPosts.layoutManager = LinearLayoutManager(this)
        recyclerTuitionPosts.itemAnimator = DefaultItemAnimator()

        // Load passed posts if any
        intent.getParcelableArrayListExtra<TuitionPost>("ALL_TUITION_POSTS")?.let { tuitionPosts.addAll(it) }
        intent.getParcelableArrayListExtra<TuitionPost>("all_tuitions")?.let { tuitionPosts.addAll(it) }

        // Adapter
        tuitionAdapter = MyHomeTuitionsAdapter(
            items = tuitionPosts,
            onApplyClick = { post -> Toast.makeText(this, "Apply clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show() },
            onFavouriteClick = { post -> Toast.makeText(this, "Favourite clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show() }
        )
        recyclerTuitionPosts.adapter = tuitionAdapter

        if (tuitionPosts.isEmpty()) loadTuitionPosts() else updateRecyclerView(tuitionPosts)



        setupSortSpinner()
        setupPagination()

        swipeRefresh.setOnRefreshListener {
            currentPage = 1
            tuitionPosts.clear()
            tuitionAdapter.notifyDataSetChanged()
            loadTuitionPosts()
        }


        backArrow.setOnClickListener { finish() }




    }

    /** Spinner Sorting */
    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_sort_item, sortOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = layoutInflater.inflate(R.layout.spinner_sort_item, parent, false)
                val textView = view.findViewById<TextView>(R.id.textSort)
                val icon = view.findViewById<ImageView>(R.id.iconSort)
                icon.visibility = if (position == 0) View.VISIBLE else View.GONE
                textView.text = getItem(position)
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = layoutInflater.inflate(R.layout.item_sort_dropdown, parent, false)
                view.findViewById<TextView>(R.id.dropdownText).text = getItem(position)
                return view
            }
        }
        spinnerSort.adapter = adapter

        spinnerSort.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (tuitionPosts.isEmpty()) return
                val selectedSort = parent?.getItemAtPosition(position).toString()
                updateRecyclerView(tuitionPosts, selectedSort)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })


    }

    /** Always keep active first, then inactive */
    /** Update RecyclerView with optional sorting (no active/inactive split) */
    private fun updateRecyclerView(posts: List<TuitionPost>, sortType: String? = null) {
        val finalList = when (sortType) {
            null, "Default" -> posts.toList()           // keep API order; make a COPY
            else -> applySort(posts, sortType)
        }
        setupRecyclerView(finalList)
    }






    /** Setup RecyclerView with posts */
    private fun setupRecyclerView(posts: List<TuitionPost>) {
        val safeCopy = posts.toList()                   // avoid aliasing with tuitionPosts
        tuitionPosts.clear()
        tuitionPosts.addAll(safeCopy)
        tuitionAdapter.notifyDataSetChanged()

        recyclerTuitionPosts.visibility = View.VISIBLE
        shimmerLayout.visibility = View.GONE
        emptyText.visibility = View.GONE
    }





    /** Sorting helpers */
    private fun applySort(posts: List<TuitionPost>, sortType: String): List<TuitionPost> {
        return when(sortType) {
            "Nearest" -> posts.sortedBy { it.distanceInKm ?: Double.MAX_VALUE }
            "Newest" -> posts.sortedByDescending { it.postedDate?.toTimeStampSafe() ?: 0L }
            "Lowest Fee" -> posts.sortedBy { it.fee?.extractFeeForSort(lowest = true) ?: Double.MAX_VALUE }
            "Highest Fee" -> posts.sortedByDescending { it.fee?.extractFeeForSort(lowest = false) ?: 0.0 }
            else -> posts
        }
    }

    private fun String.toTimeStampSafe(): Long =
        try { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(this)?.time ?: 0L } catch (e: Exception) { 0L }

    private fun String.extractFeeForSort(lowest: Boolean = true): Double {
        return try {
            val numbers = Regex("\\d+(\\.\\d+)?").findAll(this).map { it.value.toDouble() }.toList()
            if (numbers.isEmpty()) return Double.MAX_VALUE
            if (lowest) numbers.minOrNull() ?: Double.MAX_VALUE else numbers.maxOrNull() ?: 0.0
        } catch (e: Exception) {
            if (lowest) Double.MAX_VALUE else 0.0
        }
    }

    /** Pagination scroll listener */
    private fun setupPagination() {
        val layoutManager = recyclerTuitionPosts.layoutManager as LinearLayoutManager
        recyclerTuitionPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (!isLoading) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    if ((visibleItemCount + firstVisible) >= totalItemCount && firstVisible >= 0) {
                        loadNextPage()
                    }
                }
            }
        })
    }

    /** Load next page */
    private fun loadNextPage() {
        val prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()
        if (lat == null || lon == null) return

        isLoading = true
        tuitionAdapter.showLoadingFooter(true)

        RetrofitInstance.instance.getNearbyPosts(lat, lon, currentPage + 1, pageSize)
            .enqueue(object : Callback<List<TuitionPost>> {
                override fun onResponse(call: Call<List<TuitionPost>>, response: Response<List<TuitionPost>>) {
                    isLoading = false
                    tuitionAdapter.showLoadingFooter(false)
                    val posts = response.body()
                    // loadNextPage
                    if (!posts.isNullOrEmpty()) {

                        tuitionPosts.addAll(posts)
                        updateRecyclerView(tuitionPosts, spinnerSort.selectedItem?.toString())
                        currentPage++
                    }

                }

                override fun onFailure(call: Call<List<TuitionPost>>, t: Throwable) {
                    isLoading = false
                    tuitionAdapter.showLoadingFooter(false)
                }
            })
    }

    /** Initial API call */
    private fun loadTuitionPosts() {
        swipeRefresh.isRefreshing = false
        if (currentPage == 1) {
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        }
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.GONE

        val prefs = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()
        if (lat == null || lon == null) {
            showError("Location not set")
            return
        }

        RetrofitInstance.instance.getNearbyPosts(lat, lon, currentPage, pageSize)
            .enqueue(object : Callback<List<TuitionPost>> {
                override fun onResponse(call: Call<List<TuitionPost>>, response: Response<List<TuitionPost>>) {
                    swipeRefresh.isRefreshing = false
                    if (currentPage == 1) {
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                    }
                    val posts = response.body()
                    if (!posts.isNullOrEmpty()) {

                        tuitionPosts.addAll(posts)
                        updateRecyclerView(tuitionPosts, spinnerSort.selectedItem?.toString())
                    } else if (currentPage == 1) {
                        showError("Tuitions are not available near you\n\nPull to refresh.")
                    }
                }

                override fun onFailure(call: Call<List<TuitionPost>>, t: Throwable) {
                    swipeRefresh.isRefreshing = false
                    if (currentPage == 1) {
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                        showError("Failed to load tuitions. Try again\n\nPull to refresh.")
                    }
                }
            })
    }

    private fun showError(message: String) {
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}
