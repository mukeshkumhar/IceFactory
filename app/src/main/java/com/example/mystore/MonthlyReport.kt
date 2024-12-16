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
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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

class MonthlyReport : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: RecycleAdapterOrder
    private var orders = ArrayList<Order>()
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
        setContentView(R.layout.activity_monthly_report)
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


        autoCompleteTextView = findViewById(R.id.categoryNameMonthly)

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
//                    orders.addAll(latestCustomers) // Add fetched data to originalOrders
//                    adapter.updateOrders(orders)
                    withContext(Dispatchers.Main) {

                        updateChart(orders)
//                        adapter = RecycleAdapterOrder(latestCustomers)
//                        adapter.updateOrders(orders) // Add fetched data to originalOrders
//                        recyclerView.adapter = adapter

                    }


                    val ordersByMonth = orders.groupBy { order ->
                        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                        SimpleDateFormat("MMMM", Locale.getDefault()).format(date) // Get month name
                    }

                    val barChartData = ordersByMonth.mapValues { entry ->
                        // Choose either number of orders or total price:
                        entry.value.size // Number of orders
                        // or
                        entry.value.sumOf { it.value } // Total price
                    }

                    barchart = findViewById(R.id.barChartMonthly)

                } else {
                    // Handle API error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MonthlyReport, "API Error", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MonthlyReport, "Network Error", Toast.LENGTH_SHORT).show()
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
//        val categoryAdd : AutoCompleteTextView = findViewById(R.id.categoryNameMonthly)
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
//        val barDataSet = BarDataSet(list, "Monthly Report")
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

    class MyValueFormatterkg : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when {
                // Assuming dataSet is for quantity (Kg) and dataSet2 is for value (Rs)
                dataSetIndex == 0 -> " Rs ${value.toInt()}" // Add "Kg" to quantity
                else -> value.toString() // Default formatting
            }
        }

        // Optional: You can add a dataSetIndex property to track which dataset is being formatted
        var dataSetIndex: Int = 0
    }


    class MyValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return when {
                // Assuming dataSet is for quantity (Kg) and dataSet2 is for value (Rs)
                dataSetIndex == 0 -> "${value.toInt()} Kg" // Add "Rs" to value
                else -> value.toString() // Default formatting
            }
        }

        // Optional: You can add a dataSetIndex property to track which dataset is being formatted
        var dataSetIndex: Int = 0
    }




    private fun updateChart(orders: List<Order>) {

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val last5MonthsData = mutableListOf<BarEntry>()
        val totalAmounts = mutableListOf<BarEntry>()
        val valueFormatter = MyValueFormatter()
        val valueFormatterKg = MyValueFormatterkg()

        for (i in 0 until 5) {
            val monthIndex = (currentMonth - i + 12) % 12 // Handle wrapping around to previous year
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(
                Calendar.getInstance().apply { set(Calendar.MONTH, monthIndex) }.time
            )
            val totalOrdersForMonth = orders.filter { order ->
                val orderDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                Calendar.getInstance().apply { time = orderDate }.get(Calendar.MONTH) == monthIndex
            }.size



            val totalQuantityForMonth = orders.filter { order ->
                val orderDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                Calendar.getInstance().apply { time = orderDate }.get(Calendar.MONTH) == monthIndex
            }.sumOf { it.quantity } // Sum quantities for the mont



            val totalAmountForMonth = orders.filter { order ->
                val orderDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(order.createdAt)
                Calendar.getInstance().apply { time = orderDate }.get(Calendar.MONTH) == monthIndex
            }.sumOf { it.value }
            // Assuming 'value' is the amount property
//            totalAmounts[4 - i.toInt()] = totalAmountForMonth.toDouble()
//            totalAmounts.add(totalAmountForMonth.toDouble())

            println("Total Quantity for Months: $totalQuantityForMonth")
            println("Total Amount for Months:$totalAmountForMonth")

            last5MonthsData.add(BarEntry(4 - i.toFloat(), totalQuantityForMonth.toFloat())) // 4 - i for reverse order
            totalAmounts.add(BarEntry(4 - i.toFloat(), totalAmountForMonth.toFloat()))



//                           /// update 4- i.toFloat()




        }


        // 2. Create BarDataSet
        val dataSet = BarDataSet(last5MonthsData, "Monthly Total Orders")
        dataSet.valueFormatter = valueFormatter
        valueFormatter.dataSetIndex = 0

        val dataSet2 = BarDataSet(totalAmounts, "Monthly Total Amount")
        dataSet2.valueFormatter = valueFormatterKg
        valueFormatterKg.dataSetIndex = 0

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
        dataSet2.setColors(ColorTemplate.LIBERTY_COLORS,255)

        dataSet.valueTextSize = 10f
        dataSet2.valueTextSize = 10f

//        val valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
//                println("Total ammm: $totalAmounts")
//                val totalAmount = totalAmounts[value.toInt()]
//                return if (totalAmount != null) {
//                    "₹${totalAmount}"
//                } else {
//                    "" // Handle cases where total amount is not found
//                }
//            }
//        }
//
//        dataSet.valueFormatter = valueFormatter


        // 3. Create BarData and set it to the chart
        val data = BarData(dataSet2,dataSet)
//        data.barWidth = 0.3f
//        val groupSpace = 0.3f
//        val barSpace = 0.01f
        barchart = findViewById(R.id.barChartMonthly)
        barchart.data = data
//        barchart.groupBars(0f, groupSpace, barSpace)

//        data.setValueFormatter(valueFormatter)

        // 4. Customize chart appearance
        val xAxisLabels = last5MonthsData.map { SimpleDateFormat("MMMM", Locale.getDefault()).format(
            Calendar.getInstance().apply { set(Calendar.MONTH, (currentMonth - ( it.x.toInt()) + 12) % 12) }.time

            //                           /// update 4- i.toFloat()
        ) } // Get month names for x-axis labels


        barchart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barchart.xAxis.granularity = 1f
        barchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barchart.description.isEnabled = false // Hide description
        barchart.legend.isEnabled = false // Hide legend
        barchart.invalidate() // Refresh chart
        barchart.animateY(1000)
    }
}