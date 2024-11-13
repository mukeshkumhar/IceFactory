package com.example.mystore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerViewSells: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var suggestionsAdapter: RecycleAdapterSells


    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(ApiService::class.java)

    }
    //    Server link and retrofit instance handal

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val authInterceptor = AuthInterceptor(sharedPreferences)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        // Create a new apiService instance using the new Retrofit instance
        val apiServiceWithInterceptor = retrofit.create(ApiService::class.java)

//        lifecycleScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) { apiServiceWithInterceptor.getCustomers().execute() }
//                if (response.isSuccessful) {
//                    val customers = response.body() ?.toString()?: ""
//                    // Store customer data in SharedPreferences
//                    val sharedPreferences = getSharedPreferences("customer_data", Context.MODE_PRIVATE)
//                    val editor = sharedPreferences.edit()
//                    val customerJson = Gson().toJson(customers) // Convert customer list to JSON
//                    editor.putString("customers", customerJson)
//                    editor.apply()
//
//
//                    println("All Customer Store in local Storage")
//                } else {
//                    println("Failed to fetch customers: ${response.errorBody()?.string()}")
//                }
//            } catch (e: Exception) {
//                println("Error fetching customers: ${e.message}")
//            }
//        }




        val addCustomerBTN = findViewById<AppCompatButton>(R.id.addCustomerBTN)
        addCustomerBTN.setOnClickListener {
            val addCustomerPage = Intent(this, AddCustomersActivity::class.java)
            startActivity(addCustomerPage)
        }

        val dailySellsBTN = findViewById<AppCompatButton>(R.id.dailyproductBTN)
        dailySellsBTN.setOnClickListener {
            val dailySellsPage = Intent(this, DailyProductSelling::class.java)
            startActivity(dailySellsPage)
        }

        val monthlySellsBTN = findViewById<AppCompatButton>(R.id.monthlyReportBTN)
        monthlySellsBTN.setOnClickListener {
            val monthlySellsPage = Intent(this, MonthlyReport::class.java)
            startActivity(monthlySellsPage)
        }

        val yearlySellsBTN = findViewById<AppCompatButton>(R.id.yearlyReportBTN)
        yearlySellsBTN.setOnClickListener {
            val yearlySellsPage = Intent(this, YearlyReport::class.java)
            startActivity(yearlySellsPage)

        }

//        val billingBTN = findViewById<AppCompatButton>(R.id.billingBTN)
//        billingBTN.setOnClickListener {
//            val billingPage = Intent(this, Billings::class.java)
//            startActivity(billingPage)
//
//        }

        val logoutBTN = findViewById<AppCompatButton>(R.id.logoutBtn)
        logoutBTN.setOnClickListener {

            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("AccessToken")
            editor.apply()

            val loginPage = Intent(this, MainActivity::class.java)
            startActivity(loginPage)

        }

        val addCategortBTN = findViewById<AppCompatButton>(R.id.addCategoryBTN)
        addCategortBTN.setOnClickListener{
            val categoryPage = Intent(this,AddCategory::class.java)
            startActivity(categoryPage)
        }

        val allOrderBtn = findViewById<AppCompatButton>(R.id.allOrderBTN)
        allOrderBtn.setOnClickListener{
            val orderPage = Intent(this,AllOrders::class.java)
            startActivity(orderPage)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing or show a toast, this will disable the back button
        Toast.makeText(this, "Can't go back", Toast.LENGTH_SHORT).show()
    }
}