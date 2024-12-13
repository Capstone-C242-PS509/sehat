package com.example.testsehat.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testsehat.ArticleDetailActivity
import com.example.testsehat.RetrofitClient
import com.example.testsehat.Service
import com.example.testsehat.adapter.ArticleAdapter
import com.example.testsehat.data.dataArticle
import com.example.testsehat.databinding.ArticleFragmentBinding
import com.example.testsehat.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArticlesFragment : Fragment() {
    private var _binding: ArticleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var articleAdapter: ArticleAdapter
    private var originalArticleList: List<dataArticle> = listOf()
    private var fetchJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ArticleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleAdapter = ArticleAdapter { article ->
            navigateToArticleDetail(article)
        }
        binding.recyclerViewArticles.apply {
            layoutManager = GridLayoutManager(context, 2) // 2 columns grid
            adapter = articleAdapter
        }

        setupSearchView()

        checkAndFetchArticles()
    }

    private fun setupSearchView() {
        binding.searchViewArticles.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterArticles(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterArticles(query: String) {
        val filteredList = originalArticleList.filter { article ->
            article.title.contains(query, ignoreCase = true) ||
                    article.content.contains(query, ignoreCase = true)
        }
        articleAdapter.updateArticles(filteredList)
    }

    private fun navigateToArticleDetail(article: dataArticle) {
        val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
            putExtra("ARTICLE_ID", article.id)
            putExtra("ARTICLE_TITLE", article.title)
            putExtra("ARTICLE_DESCRIPTION", article.content)
            putExtra("ARTICLE_IMAGE_URL", article.url)
        }
        startActivity(intent)
    }

    private fun checkAndFetchArticles() {
        val token = context?.let { AuthManager.getToken(it) }

        if (token != null) {
            val authInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build()

            val retrofitWithAuth = Retrofit.Builder()
                .baseUrl(RetrofitClient.baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val serviceWithAuth = retrofitWithAuth.create(Service::class.java)

            fetchArticles(serviceWithAuth)
        } else {
            updateUIForNoToken()
        }
    }

    private fun fetchArticles(service: Service) {
        fetchJob?.cancel()

        fetchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Show loading state
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                }

                val response = withContext(Dispatchers.IO) {
                    service.getArticles().execute()
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        response.body()?.let { articleResponse ->
                            if (articleResponse.data.isNotEmpty()) {
                                originalArticleList = articleResponse.data
                                articleAdapter.updateArticles(originalArticleList)
                                binding.recyclerViewArticles.visibility = View.VISIBLE
                            } else {
                                updateUIForNoArticles()
                            }
                        }
                    } else {
                        handleErrorResponse(response.code())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.recyclerViewArticles.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUIForNoToken() {
        _binding?.progressBar?.visibility = View.GONE
        Toast.makeText(context, "Please log in to view articles", Toast.LENGTH_SHORT).show()
        _binding?.recyclerViewArticles?.visibility = View.GONE
    }

    private fun updateUIForNoArticles() {
        Toast.makeText(context, "No articles found", Toast.LENGTH_SHORT).show()
        binding.recyclerViewArticles.visibility = View.GONE
    }

    private fun handleErrorResponse(code: Int) {
        when (code) {
            401 -> {
                Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                context?.let { AuthManager.logout(it) }
            }
            else -> {
                Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerViewArticles.visibility = View.GONE
    }

    override fun onDestroyView() {
        fetchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}