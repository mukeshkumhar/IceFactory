package com.example.mystore

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class YearlyReport : AppCompatActivity() {

    private var Allorders = ArrayList<Order>()
    private var categories = listOf<Categoryitem>()
    private lateinit var autoCompleteTextView: AutoCompleteTextView

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

    lateinit var barchart: BarChart



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yearly_report)
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

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        autoCompleteTextView = findViewById(R.id.categoryNameYearly)

        lifecycleScope.launch {
            try {
                val response = apiServiceWithInterceptor.getCategoryItem()
                if (response.isSuccessful) {
                    categories = response.body()?.data ?: emptyList()
                    // Update AutoCompleteTextView with categories
                    withContext(Dispatchers.Main) {
                        updateAutoCompleteTextView(categories)
                    }
                } else {
                    // Handle API error
                }} catch (e: Exception) {
                // Handle network error
            }
        }


        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val response = withContext(Dispatchers.IO){ apiServiceWithInterceptor.getOrders()}
                if(response.isSuccessful){
                    println("Responce Sucessful: ${response.body()}")
                    val customerResponse = response.body()
                    val orders = customerResponse?.data ?: emptyList()
                    Allorders = ArrayList(orders)
                    val latestCustomers = orders.sortedByDescending { it.createdAt }
                    withContext(Dispatchers.Main) {
                        updateChart(orders)
                    }


                } else {
                    // Handle API error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@YearlyReport, "API Error", Toast.LENGTH_SHORT).show()
                    }
                }
                progressBar.visibility = View.GONE

            } catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@YearlyReport, "Network Error", Toast.LENGTH_SHORT).show()
                }

                println("Checking Error Responce ")

            }
            progressBar.visibility = View.GONE
        }



        // 3. Filter and Update Chart
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            println("Categorys: $categories")
            val selectedCategory = categories[position].name
            println("Selected Category: $selectedCategory")
            println("Orders: $Allorders")
            val filteredOrders = Allorders.filter { it.customer?.category?.name == selectedCategory ||
                    (it.customer?.category == null && selectedCategory == "")}
            println("Filtered Orders: $filteredOrders")
            updateChart(filteredOrders)
        }


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


//        val items = listOf("Pubs", "Coffey", "Events")
//        val categoryAdd : AutoCompleteTextView = findViewById(R.id.categoryNameYearly)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
//
//        categoryAdd.setAdapter(adapter)



//        barchart = findViewById(R.id.barChartMonthly)
//
//        val list: ArrayList<BarEntry> = ArrayList()
//
//        list.add(BarEntry(100f, 100f))
//        list.add(BarEntry(101f, 101f))
//        list.add(BarEntry(102f, 102f))
//        list.add(BarEntry(103f, 103f))
//        list.add(BarEntry(104f, 104f))
//
//        val barDataSet = BarDataSet(list, "Yearly Report")
//
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
//
//        barDataSet.valueTextColor = Color.BLACK
//
//        val barData = BarData(barDataSet)
//
//        barchart.setFitBars(true)
//
//        barchart.data = barData
//
//        barchart.description.text = "Bar Chart"
//
//        barchart.animateY(2000)
    }

    private fun updateAutoCompleteTextView(categories: List<Categoryitem>) {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        this.autoCompleteTextView.setAdapter(adapter)
    }

    private fun updateChart(orders: List<Order>) {
        val barChart = findViewById<BarChart>(R.id.barChartYearly)

        // 1. Get data for the last 5 years
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val last5YearsData = mutableListOf<BarEntry>()
        for (i in 0 until 5) {
            val year = currentYear - i

            val totalOrdersForYear = orders.filter { order ->
                val orderDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                Calendar.getInstance().apply { time = orderDate }.get(Calendar.YEAR) == year
            }.size


            val totalQuantityForYear = orders.filter { order ->
                val orderDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                Calendar.getInstance().apply { time = orderDate }.get(Calendar.YEAR) == year
            }.sumOf { it.quantity }





            last5YearsData.add(BarEntry(i.toFloat(),totalQuantityForYear.toFloat())) // 4 - i for reverse order
        }

        // 2. Create BarDataSet
        val dataSet = BarDataSet(last5YearsData, "Yearly Total Orders")
        dataSet.color = Color.BLUE

        // 3. Create BarData and set it to the chart
        val data = BarData(dataSet)
        barChart.data = data

        // 4. Customize chart appearance
        val xAxisLabels = last5YearsData.map { (currentYear - ( it.x.toInt())).toString() } // Get year values for x-axis labels

        ///Edited at (4- it.x.toInt())).toString()

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.description.isEnabled = false // Hide description
        barChart.legend.isEnabled = false // Hide legend
        barChart.invalidate() // Refresh chart
        barChart.animateY(1000)
    }
}