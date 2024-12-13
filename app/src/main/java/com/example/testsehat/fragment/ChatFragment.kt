    package com.example.testsehat.fragment

    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.testsehat.adapter.ChatAdapter
    import com.example.testsehat.databinding.ChatFragmentBinding
    import dataChat
    import ChatDetails
    import com.example.testsehat.data.ApiRequest
    import com.example.testsehat.data.ApiResponse
    import com.example.testsehat.RetrofitClient
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import android.os.Handler
    import android.os.Looper
    import android.util.Log
    import com.example.testsehat.AuthManager
    import com.example.testsehat.data.MentalDiseaseData
    import com.example.testsehat.RetrofitClientModel
    import com.example.testsehat.Service
    import okhttp3.OkHttpClient
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory

    class ChatFragment : Fragment() {
        private var _binding: ChatFragmentBinding? = null
        private val binding get() = _binding!!
        private val messages = mutableListOf<dataChat>()
        private lateinit var adapter: ChatAdapter
        private val handler = Handler(Looper.getMainLooper())
        private var delay = 0L

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = ChatFragmentBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            setupRecyclerView()
            setupSendButton()
        }

        private fun setupRecyclerView() {
            adapter = ChatAdapter(messages)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter
        }

        private fun setupSendButton() {
            binding.btnSend.setOnClickListener {
                val userMessage = binding.etInputText.text.toString().trim()
                if (userMessage.isNotBlank()) {
                    addMessage(dataChat(userMessage, true))
                    binding.etInputText.text.clear()

                    sendApiRequest(ApiRequest(text = userMessage))
                } else {

                    addMessage(dataChat("Please fill the text", false))
                }
            }

        }

        private fun addMessage(message: dataChat) {
            handler.postDelayed({
                messages.add(message)
                adapter.notifyItemInserted(messages.size -1)
                binding.recyclerView.scrollToPosition(messages.size - 1)
                delay += 1000
            }, delay)
        }

        private fun sendApiRequest(request: ApiRequest) {
            val api = RetrofitClientModel.instance

            api.getPrediction(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val chatDetails = ChatDetails(
                                prediction = body.predictions ?: "No prediction available",
                                description = body.description ?: "No description available",
                                firstAid = body.first_aid ?: emptyList(),
                                improvementTips = body.improvement_tips ?: emptyList()
                            )

                            addMessage(dataChat("Prediction : ${chatDetails.prediction}", false))
                            addMessage(dataChat("Description : ${chatDetails.description}", false))

                            if (chatDetails.firstAid.isNotEmpty()) {
                                val firstAidList = chatDetails.firstAid.joinToString("\n") { it }
                                addMessage(dataChat("First Aid :\n$firstAidList", false))
                            }

                            if (chatDetails.improvementTips.isNotEmpty()) {
                                val improvementTipsList = chatDetails.improvementTips.joinToString("\n") { it }
                                addMessage(dataChat("Solution :\n$improvementTipsList", false))
                            }
                            postMentalDiseaseSummary(chatDetails.prediction)
                        } else {
                            addMessage(dataChat("Error: Empty response body", false))
                        }
                    } else {
                        addMessage(dataChat("Error: ${response.code()}", false))
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    addMessage(dataChat("Error: ${t.message}", false))
                }
            })
        }

        private fun postMentalDiseaseSummary(prediction: String) {
            val mentalDiseaseData = MentalDiseaseData(mental_disease = prediction)

            val token = context?.let { AuthManager.getToken(it) }

            if (token != null) {
                val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                        chain.proceed(newRequest)
                    }
                    .build()

                val retrofitWithAuth = Retrofit.Builder()
                    .baseUrl(RetrofitClient.baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val serviceWithAuth = retrofitWithAuth.create(Service::class.java)

                serviceWithAuth.postSummary(mentalDiseaseData).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Log.d("API_RESPONSE", "Summary Posted Successfully: ${response.body()}")


                        } else {
                            Log.e("API_RESPONSE", "Failed to post summary: ${response.code()}")
                                                    }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Log.e("API_RESPONSE", "Network error when posting summary", t)
                        addMessage(dataChat("Network error posting summary", false))
                    }
                })
            } else {
                addMessage(dataChat("Please log in to post summary", false))
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
