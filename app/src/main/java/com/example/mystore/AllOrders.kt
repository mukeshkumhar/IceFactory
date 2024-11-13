package com.example.mystore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AllOrders : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: RecycleAdapterOrder
    private var orders = ArrayList<Order>()

    private var originalOrders: List<Order> = emptyList()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(ApiService::class.java)

    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_orders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val authInterceptor = AuthInterceptor(sharedPreferences)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .readTimeout(500, TimeUnit.SECONDS) // Set read timeout
            .connectTimeout(500, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        // Create a new apiService instance using the new Retrofit instance
        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)




        recyclerView = findViewById<RecyclerView>(R.id.recycleViewOrder)
        searchView = findViewById<SearchView>(R.id.searchTxt)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RecycleAdapterOrder(orders)
        recyclerView.adapter = adapter

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterOrders(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterOrders(newText)
                return true
            }


        })







        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val response = withContext(Dispatchers.IO){ apiServiceWithInterceptor.getOrders()}
                if(response.isSuccessful){
                    println("Responce Sucessful: ${response.body()}")
                    val customerResponse = response.body()
                    val orders = customerResponse?.data ?: emptyList()
                    val latestCustomers = orders.sortedByDescending { it.createdAt }


                    originalOrders = latestCustomers // Store fetched data in originalOrders
                    adapter = RecycleAdapterOrder(originalOrders) // Initialize adapter with original data
                    recyclerView.adapter = adapter


//                    orders.addAll(latestCustomers) // Add fetched data to originalOrders
//                    adapter.updateOrders(orders)
                    withContext(Dispatchers.Main) {
                        adapter = RecycleAdapterOrder(latestCustomers)
                        adapter.updateOrders(orders) // Add fetched data to originalOrders
                        recyclerView.adapter = adapter
                    }
                } else {
                    // Handle API error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AllOrders, "API Error", Toast.LENGTH_SHORT).show()
                    }

                    println("Checking Error Responce: ${response}   ......\n  ..... ${authInterceptor}")
                }

            } catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AllOrders, "Network Error", Toast.LENGTH_SHORT).show()
                }

                println("Checking Error Responce ")
            }
            progressBar.visibility = View.GONE
        }

















        val backBTN = findViewById<ImageButton>(R.id.backBtn)
        backBTN.setOnClickListener{
            val homePage = Intent(this,HomeActivity::class.java)
            startActivity(homePage)
        }
        val logoutBTN = findViewById<AppCompatButton>(R.id.logoutBtn)
        logoutBTN.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("AccessToken")
            editor.apply()

            val loginPage = Intent(this, MainActivity::class.java)
            startActivity(loginPage)

        }
    }

    private fun filterOrders(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            originalOrders
        } else {
            originalOrders.filter { order ->
                order.customer.fullName.contains(query, ignoreCase = true) ||
                        order.customer.email.contains(query, ignoreCase = true)
            }
        }
        adapter.updateOrders(filteredList) // Update adapter with filtered list
    }
}