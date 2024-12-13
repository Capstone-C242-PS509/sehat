package com.example.testsehat.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testsehat.ArticleDetailActivity
import com.example.testsehat.AuthManager
import com.example.testsehat.R
import com.example.testsehat.RetrofitClient
import com.example.testsehat.Service
import com.example.testsehat.adapter.FeaturedArticleAdapter
import com.example.testsehat.adapter.SummaryAdapter
import com.example.testsehat.data.*
import com.example.testsehat.databinding.HomeFragmentBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log

class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var featuredArticleAdapter: FeaturedArticleAdapter
    private lateinit var summaryAdapter: SummaryAdapter
    private lateinit var musicAdapter: MusicAdapter

    private var articleFetchJob: Job? = null
    private var diseasesFetchJob: Job? = null
    private var musicFetchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeRecyclerViews()

        setupListeners()

        checkAndFetchData()
    }

    private fun initializeRecyclerViews() {
        summaryAdapter = SummaryAdapter()
        binding.recyclerViewMentalDiseases.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = summaryAdapter
        }

        featuredArticleAdapter = FeaturedArticleAdapter { article ->
            navigateToArticleDetail(article)
        }
        binding.recyclerViewFeaturedArticles.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredArticleAdapter
        }

        musicAdapter = MusicAdapter { music ->
            openSpotifyTrack(music.url)
        }
        binding.recyclerViewMusic.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = musicAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSeeAllArticles.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ArticlesFragment())
                .commit()
        }

        setupMoodSpinner()
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

    private fun checkAndFetchData() {
        val token = context?.let { AuthManager.getToken(it) }

        if (token != null) {
            val serviceWithAuth = createAuthenticatedService(token)

            fetchDataInParallel(serviceWithAuth)
        } else {
            Toast.makeText(context, "Please log in to view content", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAuthenticatedService(token: String): Service {
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

        return retrofitWithAuth.create(Service::class.java)
    }

    private fun fetchDataInParallel(service: Service) {
        articleFetchJob?.cancel()
        diseasesFetchJob?.cancel()

        articleFetchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val articlesResponse = withContext(Dispatchers.IO) {
                    service.getArticles().execute()
                }

                withContext(Dispatchers.Main) {
                    handleArticlesResponse(articlesResponse)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Articles fetch error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        diseasesFetchJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val diseasesResponse = withContext(Dispatchers.IO) {
                    service.getMentalDiseases().execute()
                }

                withContext(Dispatchers.Main) {
                    handleMentalDiseasesResponse(diseasesResponse)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Diseases fetch error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleArticlesResponse(response: retrofit2.Response<ArticleResponse>) {
        if (response.isSuccessful) {
            response.body()?.let { articleResponse ->
                if (articleResponse.data.isNotEmpty()) {
                    val featuredArticles = articleResponse.data.shuffled().take(4)
                    featuredArticleAdapter.updateArticles(featuredArticles)
                } else {
                    Toast.makeText(context, "No articles found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMentalDiseasesResponse(response: retrofit2.Response<MentalDiseaseResponse>) {
        if (response.isSuccessful) {
            val diseaseResponse = response.body()
            diseaseResponse?.data?.let { diseaseData ->
                if (diseaseData.isNotEmpty()) {
                    summaryAdapter.updateDiseases(diseaseData)
                    setupPieChart(diseaseData)
                } else {
                    Log.d("API_RESPONSE", "Empty disease data list")
                }
            } ?: run {
                Log.d("API_RESPONSE", "Disease data is null")
            }
        } else {
            Log.e("API_RESPONSE", "Unsuccessful response: ${response.code()}")
        }
    }

    private fun setupMoodSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.moods,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.moodSpinner.adapter = adapter
        }

        binding.moodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedMood = parent.getItemAtPosition(pos).toString()

                if (pos > 0) {
                    fetchMusicForMood(selectedMood)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchMusicForMood(mood: String) {
        musicFetchJob?.cancel()

        val token = context?.let { AuthManager.getToken(it) }

        if (token != null) {
            musicFetchJob = viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val serviceWithAuth = createAuthenticatedService(token)

                    val response = withContext(Dispatchers.IO) {
                        serviceWithAuth.getMusic(mood).execute()
                    }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            response.body()?.let { musicList ->
                                if (musicList.isNotEmpty()) {
                                    musicAdapter.updateMusic(musicList)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No music found for this mood",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to load music",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Network error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Please log in to view content", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPieChart(diseaseData: List<MentalDiseaseData>) {
        val pieChart = binding.pieChartMentalDiseases
        pieChart.clear()

        val entries = diseaseData.map { PieEntry(it.count?.toFloat() ?: 0f, it.mental_disease ?: "Unknown") }

        val dataSet = PieDataSet(entries, "Mental Health Insights")
        dataSet.colors = listOf(
            Color.rgb(102, 178, 255),  // Light Blue
            Color.rgb(0, 128, 255),    // Bright Blue
            Color.rgb(0, 51, 102)      // Dark Blue
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Mental Health"
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun openSpotifyTrack(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.spotify.music")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://open.spotify.com/track/" + url.split(":").last())
            )
            startActivity(browserIntent)
        }
    }

    override fun onDestroyView() {
        articleFetchJob?.cancel()
        diseasesFetchJob?.cancel()
        musicFetchJob?.cancel()

        super.onDestroyView()
        _binding = null
    }
}