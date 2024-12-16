package com.example.mystore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Date

class CustomerData : AppCompatActivity() {

    private lateinit var DatePicker: DatePicker
    private var startDate: Date? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_data)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backBTN = findViewById<ImageButton>(R.id.backBTN)
        backBTN.setOnClickListener {
            val dailyProduct = Intent(this, DailyProductSelling::class.java)
            startActivity(dailyProduct)
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

        val customerDate = findViewById<DatePicker>(R.id.datePicker)
        val customerWeight = findViewById<EditText>(R.id.customerWeight)
        val email = intent.getStringExtra("email")


        val customerEmail = findViewById<TextView>(R.id.customerEmail)

        customerEmail.text = email

        val addBtn = findViewById<AppCompatButton>(R.id.addBtn)

        DatePicker = findViewById(R.id.datePicker)

        DatePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->

            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            startDate = calendar.time

//            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val formattedStartDate = formatter.format(startDate)
//            val formatterDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val fformattedStartDate = formatter.parse(formattedStartDate)
//            startDateCalendar = fformattedStartDate

            println("Start Date: $startDate")
        }
        val loadingBar = findViewById<ProgressBar>(R.id.lodingBar)

        addBtn.setOnClickListener {
            loadingBar.visibility = View.VISIBLE

            val weight = customerWeight.text.toString().toDoubleOrNull()?: 0.0
            val custemerEmail = email.toString()
            val day = DatePicker.dayOfMonth
            val month = DatePicker.month
            val year = DatePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val Date = calendar.time // This is your Date object




            val data = CustomerDataWeight(custemerEmail, weight,Date)

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO){apiServiceWithInterceptor.createOrder(data).execute()}
                    if (response.isSuccessful && response.body() != null) {
                        // Customer added successfully
                        loadingBar.visibility = View.GONE
                        Toast.makeText(this@CustomerData, "Weight added!", Toast.LENGTH_SHORT).show()
                        // Optionally clear the input fields or navigate to another screen
                    } else {
                        // Handle error
                        loadingBar.visibility = View.GONE
                        val errorMessage = response.body()?.message ?: "Failed to add Weight"
                        Toast.makeText(this@CustomerData, errorMessage, Toast.LENGTH_SHORT).show()}
                } catch (e: Exception){
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@CustomerData, "Error in Data Feching..: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("Error in Data Feching..: ${e.message}"+"----")
                }
            }
        }

    }
}