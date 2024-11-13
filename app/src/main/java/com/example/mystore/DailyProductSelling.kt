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
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit



class DailyProductSelling : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: RecycleAdapterSells
    private var customers = ArrayList<Customer>()

    private var originalOrders: List<Customer> = emptyList()



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
    //    Server link and retrofit instance handal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daily_product_selling)
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



        recyclerView = findViewById<RecyclerView>(R.id.recycleViewDailySells)
        searchView = findViewById<SearchView>(R.id.searchTxt)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        addDataToList()

        adapter = RecycleAdapterSells(customers)
        recyclerView.adapter = adapter

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })


        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val response = withContext(Dispatchers.IO){ apiServiceWithInterceptor.getCustomers()}
                if(response.isSuccessful){
                    println("Responce Sucessful: ${response.body()}")
                    val customerResponse = response.body()
                    val customers = customerResponse?.data ?: emptyList()
                    val latestCustomers = customers.sortedByDescending { it.createdAt }

                    originalOrders = latestCustomers // Store fetched data in originalOrders
                    adapter = RecycleAdapterSells(originalOrders) // Initialize adapter with original data
                    recyclerView.adapter = adapter

                    withContext(Dispatchers.Main) {
                        adapter = RecycleAdapterSells(latestCustomers)
                        adapter.updateOrders(customers)
                        recyclerView.adapter = adapter
                    }
                } else {
                    // Handle API error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DailyProductSelling, "API Error", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DailyProductSelling, "Network Error", Toast.LENGTH_SHORT).show()
                }

                println("Checking Error Responce ")
        }
            progressBar.visibility = View.GONE
        }



//        adapter.onItemClick = {
//            println("Item was Clicked: ${it.email}")
//            val intent = Intent(this, CustomerData::class.java)
//            intent.putExtra("email", it.email)
//            startActivity(intent)
//        }

        val backBTN = findViewById<ImageButton>(R.id.backBTN)
        backBTN.setOnClickListener {
            val homePage = Intent(this, HomeActivity::class.java)
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



    private  fun filterList(query: String?){

        val filteredList = if (query.isNullOrEmpty()) {
            originalOrders
        } else {
            originalOrders.filter { customerr ->
                customerr.fullName.contains(query, ignoreCase = true) ||
                        customerr.email.contains(query, ignoreCase = true)
            }
        }
        adapter.updateOrders(filteredList) // Update adapter with filtered list
//        if(query != null){
//            val filteredList = ArrayList<Customer>()
//            for(i in customers){
//                if (i.fullName.lowercase(Locale.ROOT).contains(query)){
//                    filteredList.add(i)
//                    }
//            }
//            if (filteredList.isEmpty()){
//                Toast.makeText(this@DailyProductSelling,"No data found", Toast.LENGTH_SHORT).show()
//            } else {
//                adapter.setFilteredList(filteredList)
//            }
//        }
    }


//    private  fun addDataToList() {
//        customers.add(Customer("2024-08-04","Mukesh","mukesh@gmail.com","2024-08-04","2024-08-04","2024-08-04",0,))
//        customers.add(Customer("2024-08-03","cksdjhk","mudcjsdkj@gmail.com","2024-08-03","2024-08-03","2024-08-03",0,))
//        customers.add(Customer("2024-08-02","sdhcjgdsesh","hellesh@gmail.com","2024-08-02","2024-08-02","2024-08-02",0,))
//
//
//        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        val authInterceptor = AuthInterceptor(sharedPreferences)
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .readTimeout(50, TimeUnit.SECONDS) // Set read timeout
//            .connectTimeout(50, TimeUnit.SECONDS)
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(RetrofitInstance.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//
//        // Create a new apiService instance using the new Retrofit instance
//        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)
//
//        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
//
//
//
//
//
//
//
//
//
//
//
//
////        println("lifecycleScope")
////        lifecycleScope.launch {
////            try {
////                println("lifecycleScope try inside")
////                progressBar.visibility = View.VISIBLE // Show loading indicator
////                val response = withContext(Dispatchers.IO) {
////                    apiServiceWithInterceptor.getCustomers()
////                }
////                println("lifecycleScope if inside")
////                if (response.isSuccessful) {
////
////                    val customers = response.body() ?: emptyList()
////                    val letestCustomer = customers.sortedByDescending { it.createdAt }.take(10)
////                    withContext(Dispatchers.Main) {
////                        recyclerView.adapter = recycleAdapter
////                    }
////                } else {
////
////                    println("Not get response from DailyProductSelling")
////                    // Handle error
////                }
////            } catch (e: Exception) {
////                println("Exception in DailyProduectSelling: ${e.message}")
////                // Handle exception
////            }finally {
////                progressBar.visibility = View.GONE // Hide loading indicator
////            }
////        }
//    }


}