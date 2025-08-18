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


class TutorHomeFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView
    private lateinit var locationText: TextView
    private lateinit var locationSelector: LinearLayout
    private lateinit var locationLauncher: ActivityResultLauncher<Intent>
    private lateinit var instituteVacancyRecycler: RecyclerView
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyText: TextView
    private lateinit var recyclerTuitionPosts: RecyclerView
    private lateinit var viewAllTuition: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        instituteVacancyRecycler = view.findViewById(R.id.recyclerInstituteVacancy)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyText = view.findViewById(R.id.emptyText)
        viewAllTuition = view.findViewById(R.id.view_all_tuition)

        // RecyclerView setup
        recyclerTuitionPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerTuitionPosts.itemAnimator = DefaultItemAnimator()

        instituteVacancyRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Location selector click
        locationSelector.setOnClickListener {
            val token = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("token", null)
            val intent = Intent(requireContext(), LocationSelectActivity::class.java)
            intent.putExtra("token", token)
            locationLauncher.launch(intent)
        }

        notificationButton.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        creditCoin.setOnClickListener {
            Toast.makeText(requireContext(), "Credits: ${creditBalance.text}", Toast.LENGTH_SHORT).show()
        }
        viewAllTuition.setOnClickListener {
            val intent = Intent(requireContext(), AllTuitionPostsActivity::class.java)
            startActivity(intent)
        }


        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener { loadTuitionPosts() }

        // Load cached location
        loadCachedLocation()

        // Load tuition posts
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

            val location = LocationHelper.LocationData(
                lat, lon,
                sublocality ?: "",
                area,
                city,
                state ?: "Unknown State",
                country ?: "India"
            )

            lifecycleScope.launch {
                LocationHelper.sendLocationToBackend(requireContext(), location)
            }
        }
    }

    private fun loadTuitionPosts() {
        swipeRefresh.isRefreshing = true
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        recyclerTuitionPosts.visibility = View.GONE
        emptyText.visibility = View.GONE

        val prefs = requireContext().getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()

        if (lat == null || lon == null) {
            Toast.makeText(requireContext(), "Location not set", Toast.LENGTH_SHORT).show()
            swipeRefresh.isRefreshing = false
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            return
        }

        val api = RetrofitInstance.instance
        val call = api.getNearbyPosts(lat, lon)

        call.enqueue(object : retrofit2.Callback<List<TuitionPost>> {
            override fun onResponse(
                call: retrofit2.Call<List<TuitionPost>>,
                response: retrofit2.Response<List<TuitionPost>>
            ) {
                swipeRefresh.isRefreshing = false
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE

                val tuitionPosts = response.body()
                if (response.isSuccessful && !tuitionPosts.isNullOrEmpty()) {

                    // Filter only active posts first
                    val activePosts = tuitionPosts.filter { it.status == "active" }

                    // Show only first 3 in home
                    val limitedList = if (activePosts.size > 4) activePosts.take(4) else activePosts

                    val tuitionAdapter = MyHomeTuitionsAdapter(
                        items = limitedList,
                        showViewAll = true,
                        onApplyClick = { post ->
                            Toast.makeText(requireContext(), "Apply clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show()
                        },
                        onFavouriteClick = { post ->
                            Toast.makeText(requireContext(), "Favourite clicked: ${post.tuitionCode}", Toast.LENGTH_SHORT).show()
                        },
                        onViewAllClick = {
                            val intent = Intent(requireContext(), AllTuitionPostsActivity::class.java)
                            intent.putParcelableArrayListExtra("all_tuitions", ArrayList(tuitionPosts))
                            startActivity(intent)
                        }
                    )

                    recyclerTuitionPosts.adapter = tuitionAdapter
                    recyclerTuitionPosts.visibility = View.VISIBLE
                    emptyText.visibility = View.GONE
                }
                else {
                    recyclerTuitionPosts.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Tuitions are not available near you.\nPull to refresh."
                }
            }

            override fun onFailure(call: retrofit2.Call<List<TuitionPost>>, t: Throwable) {
                swipeRefresh.isRefreshing = false
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                recyclerTuitionPosts.visibility = View.GONE
                emptyText.visibility = View.VISIBLE
                emptyText.text = "Failed to load tuitions. Try again.\n" + "Pull to reload."
            }
        })
    }

}
