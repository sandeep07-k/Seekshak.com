package com.example.seekshakcom.tutor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seekshakcom.R
import com.example.seekshakcom.adapter.MyHomeTuitionsAdapter
import com.example.seekshakcom.model.TuitionPost
import com.example.seekshakcom.network.RetrofitInstance
import com.example.seekshakcom.student.LocationSelectActivity
import com.example.seekshakcom.utils.LocationHelper
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TutorHomeFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView
    private lateinit var locationText: TextView
    private lateinit var locationSelector: LinearLayout
    private lateinit var locationLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerTuitionPosts: RecyclerView
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyText: TextView
    private lateinit var viewAllTuition: LinearLayout

    private var allTuitionPosts: ArrayList<TuitionPost> = arrayListOf()  // full list
    private var tuitionPosts: ArrayList<TuitionPost> = arrayListOf()     // preview only
    private lateinit var tuitionAdapter: MyHomeTuitionsAdapter
    private var isLoading = false
    private var currentPage = 1
    private val pageSize = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedCity = data?.getStringExtra("selected_city") ?: ""
                val selectedArea = data?.getStringExtra("selected_area") ?: ""
                val selectedSublocality = data?.getStringExtra("selected_sublocality") ?: ""

                locationText.text = if (selectedSublocality.isNotEmpty())
                    "$selectedArea, $selectedCity"
                else
                    "$selectedArea, $selectedCity"

                val prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("selected_city", selectedCity)
                    putString("selected_area", selectedArea)
                    putString("selected_sublocality", selectedSublocality)
                    apply()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tutor_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        searchEditText = view.findViewById(R.id.searchEditText)
        notificationButton = view.findViewById(R.id.notification_Btn)
        creditBalance = view.findViewById(R.id.credit_balance)
        creditCoin = view.findViewById(R.id.credit_coin)
        locationText = view.findViewById(R.id.locationText)
        locationSelector = view.findViewById(R.id.locationSelector)
        recyclerTuitionPosts = view.findViewById(R.id.recyclerTuitionPosts)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyText = view.findViewById(R.id.emptyText)
        viewAllTuition = view.findViewById(R.id.view_all_tuition)

        // RecyclerView setup
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerTuitionPosts.layoutManager = layoutManager
        recyclerTuitionPosts.itemAnimator = DefaultItemAnimator()

        // Adapter init
        tuitionAdapter = MyHomeTuitionsAdapter(
            items = tuitionPosts,
            showViewAll = true,
            onApplyClick = { post -> Toast.makeText(requireContext(), "Apply clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show() },
            onFavouriteClick = { post -> Toast.makeText(requireContext(), "Favourite clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show() },
            onViewAllClick = {
                val intent = Intent(requireContext(), AllTuitionPostsActivity::class.java)
                intent.putParcelableArrayListExtra("all_tuitions", ArrayList(allTuitionPosts)) // full list
                startActivity(intent)

            }
        )
        recyclerTuitionPosts.adapter = tuitionAdapter

        // Location selector
        locationSelector.setOnClickListener {
            val token = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("token", null)
            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.putExtra("token", token)
            locationLauncher.launch(intent)
        }

        notificationButton.setOnClickListener { Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show() }
        creditCoin.setOnClickListener { Toast.makeText(requireContext(), "Credits: ${creditBalance.text}", Toast.LENGTH_SHORT).show() }
        viewAllTuition.setOnClickListener {
            val intent = Intent(requireContext(), AllTuitionPostsActivity::class.java)
            intent.putParcelableArrayListExtra("all_tuitions", ArrayList(allTuitionPosts)) // full list
            startActivity(intent)
        }

        swipeRefresh.setOnRefreshListener {
            currentPage = 1
            tuitionPosts.clear()
            tuitionAdapter.notifyDataSetChanged()
            loadTuitionPosts()
        }

        loadCachedLocation()
        loadTuitionPosts()
    }

    private fun loadCachedLocation() {
        val prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val city = prefs.getString("selected_city", null)
        val area = prefs.getString("selected_area", null)
        val sublocality = prefs.getString("selected_sublocality", null)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()
        val state = prefs.getString("selected_state", null)
        val country = prefs.getString("selected_country", null)

        if (!city.isNullOrEmpty() && !area.isNullOrEmpty() && lat != null && lon != null) {
            locationText.text = if (!sublocality.isNullOrEmpty()) "$area, $city" else "$area, $city"
            val location = LocationHelper.LocationData(lat, lon, sublocality ?: "", area, city, state ?: "Unknown State", country ?: "India")
            lifecycleScope.launch { LocationHelper.sendLocationToBackend(requireContext(), location) }
        }
    }

    private fun loadTuitionPosts() {
        swipeRefresh.isRefreshing = false
        if (currentPage == 1) {
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        }
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.GONE

        val prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()
        if (lat == null || lon == null) {
            showError("Location not set")
            return
        }

        isLoading = true
        RetrofitInstance.instance.getNearbyPosts(lat, lon, 1, pageSize) // always page 1 for preview
            .enqueue(object : Callback<List<TuitionPost>> {
                override fun onResponse(call: Call<List<TuitionPost>>, response: Response<List<TuitionPost>>) {
                    isLoading = false
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE

                    val posts = response.body()
                    if (!posts.isNullOrEmpty()) {
                        val activePosts = posts.filter { it.status == "active" }
                        val inactivePosts = posts.filter { it.status != "active" }

                        // ✅ Full list always stored here
                        allTuitionPosts.clear()
                        allTuitionPosts.addAll(activePosts + inactivePosts)

                        // 👉 Preview list only for Home screen
                        val previewList = if (activePosts.size > 3) activePosts.take(3) else activePosts
                        tuitionAdapter.updateItems(ArrayList(previewList)) // show only 3 here

                        recyclerTuitionPosts.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE
                    } else {
                        showError("Tuitions are not available near you.\nPull to refresh.")
                    }

                }

                override fun onFailure(call: Call<List<TuitionPost>>, t: Throwable) {
                    isLoading = false
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    showError("Failed to load tuitions. Try again.\nPull to reload.")
                }
            })
    }

    private fun showError(message: String) {
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}
