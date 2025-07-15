package com.example.seekshakcom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.seekshakcom.model.MyPost
import com.example.seekshakcom.network.ApiClient
import com.example.seekshakcom.network.ApiResponse
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: TextView
    private lateinit var myPostAdapter: MyPostAdapter
    private val postList = mutableListOf<MyPost>()

    private lateinit var editPostLauncher: ActivityResultLauncher<Intent>



    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private val limitPerPage = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_posts, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        emptyView = view.findViewById(R.id.emptyView)

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemInsets.bottom + 32)
            WindowInsetsCompat.CONSUMED
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myPostAdapter = MyPostAdapter(
            postList,
            onRepost = { post -> repostPost(post._id) },
            onMarkFilled = { post -> markAsFilled(post._id) },
            onRemove = { post -> removePost(post._id) },
            onEdit = { post -> editPost(post) }
        )
        recyclerView.adapter = myPostAdapter
        val bottomPadding = resources.getDimensionPixelSize(R.dimen.extra_bottom_spacing)
        recyclerView.addItemDecoration(BottomSpacingItemDecoration(bottomPadding))

        swipeRefreshLayout.setOnRefreshListener {
            currentPage = 1
            isLastPage = false
            loadMyPosts(1)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !isLoading && !isLastPage) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val visibleCount = lm.childCount
                    val totalCount = lm.itemCount
                    val firstPos = lm.findFirstVisibleItemPosition()
                    if ((visibleCount + firstPos) >= totalCount - 2) {
                        loadMyPosts(currentPage + 1)
                    }
                }
            }
        })

        editPostLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPage = 1
                isLastPage = false
                loadMyPosts(1)
            }
        }




        showShimmer(true)
        loadMyPosts(1)

        return view
    }

    private fun loadMyPosts(page: Int) {
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            swipeRefreshLayout.isRefreshing = false
            showShimmer(false)
            showEmptyAnimated()
            return
        }

        isLoading = true
        if (page == 1) showShimmer(true)

        ApiClient.instance.getMyPosts(userId, page, limitPerPage)
            .enqueue(object : Callback<List<MyPost>> {
                override fun onResponse(
                    call: Call<List<MyPost>>,
                    response: Response<List<MyPost>>
                ) {
                    swipeRefreshLayout.isRefreshing = false
                    showShimmer(false)
                    isLoading = false

                    if (response.isSuccessful && response.body() != null) {
                        val newPosts = response.body()!!

                        if (page == 1) postList.clear()
                        postList.addAll(newPosts)
                        myPostAdapter.notifyDataSetChanged()

                        currentPage = page
                        isLastPage = newPosts.size < limitPerPage
                    }

                    if (postList.isEmpty()) {
                        showEmptyAnimated()
                    } else {
                        emptyView.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<MyPost>>, t: Throwable) {
                    swipeRefreshLayout.isRefreshing = false
                    showShimmer(false)
                    isLoading = false
                    Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()

                    if (postList.isEmpty()) {
                        showEmptyAnimated()
                    } else {
                        emptyView.visibility = View.GONE
                    }
                }
            })
    }

    private fun showShimmer(isLoading: Boolean) {
        if (isLoading) {
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
        } else {
            shimmerLayout.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    shimmerLayout.alpha = 1f

                    recyclerView.alpha = 0f
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setListener(null)
                }
        }
    }

    private fun showEmptyAnimated() {
        emptyView.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(400)
                .setListener(null)
        }
    }

    private fun repostPost(postId: String) {
        ApiClient.instance.reactivatePost(postId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Post reposted", Toast.LENGTH_SHORT).show()
                    loadMyPosts(1)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to repost: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markAsFilled(postId: String) {
        ApiClient.instance.markPostFilled(postId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Post marked as filled", Toast.LENGTH_SHORT).show()
                    loadMyPosts(1)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to mark as filled: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removePost(postId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Post")
            .setMessage("Are you sure you want to remove this post?")
            .setPositiveButton("Yes") { _, _ ->
                ApiClient.instance.deletePost(postId).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(context, "Post removed", Toast.LENGTH_SHORT).show()
                            loadMyPosts(1)
                        } else {
                            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(context, "Failed to remove post: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun editPost(post: MyPost) {
        val intent = Intent(requireContext(), EditPostActivity::class.java)
        intent.putExtra("POST_ID", post._id)
        intent.putExtra("POST_DATA", post)
        editPostLauncher.launch(intent)  // ✅ Instead of startActivity()
    }


//    private fun editPost(post: MyPost) {
//        val intent = Intent(requireContext(), EditPostActivity::class.java).apply {
//            putExtra("POST_ID", post._id)
//            putExtra("POST_DATA", post)
//        }
//        editPostLauncher.launch(intent)
//    }



}
