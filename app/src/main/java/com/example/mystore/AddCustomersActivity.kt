package com.example.mystore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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

class AddCustomersActivity : AppCompatActivity() {


    private lateinit var autoCompleteTextView: AutoCompleteTextView
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
        setContentView(R.layout.activity_add_customers)
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

        autoCompleteTextView = findViewById(R.id.categoryName)

        lifecycleScope.launch {
            try {
                val response = apiServiceWithInterceptor.getCategoryItem()
                if (response.isSuccessful) {
                    val categories = response.body()?.data ?: emptyList()
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


//        val items = listOf("Pubs", "Coffey", "Events")
//        val categoryAdd : AutoCompleteTextView = findViewById(R.id.categoryName)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
//
//        categoryAdd.setAdapter(adapter)
//
//        categoryAdd.onItemClickListener = AdapterView.OnItemClickListener {
//            adapterView, view, i, l ->
//
//            val itemSelected = adapterView.getItemAtPosition(i)
//            Toast.makeText(this, "Item: $itemSelected", Toast.LENGTH_SHORT).show()
//        }








        val customerName = findViewById<EditText>(R.id.customerName)
        val customerEmail = findViewById<EditText>(R.id.customerEmail)
        val categoryItem = findViewById<AutoCompleteTextView>(R.id.categoryName)
        val addCustomerBTN = findViewById<AppCompatButton>(R.id.addBtn)
        val loadingBar = findViewById<ProgressBar>(R.id.progressBar1)

        addCustomerBTN.setOnClickListener {
            loadingBar.visibility = View.VISIBLE
            val name = customerName.text.toString()
            val email = customerEmail.text.toString()
            val category = categoryItem.text.toString()

            val customer = CustomerAdd(name,email,category)

            lifecycleScope.launch {


                try {

                    val response = withContext(Dispatchers.IO){ apiServiceWithInterceptor.addCustomer(customer).execute()}
                    if (response.isSuccessful && response.body() != null) {

                        val customerResponse = response.body()


                        if (customerResponse?.success == true) {

                            // Customer added successfully
                            loadingBar.visibility = View.GONE
                            Toast.makeText(
                                this@AddCustomersActivity,
                                customerResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            // Optionally clear the input fields or navigate to another screen
                        } else {
                            // Handle error
                            loadingBar.visibility = View.GONE
                            val errorMessage = response.body()?.message ?: "Failed to add customer"
                            Toast.makeText(
                                this@AddCustomersActivity,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else {
                        withContext(Dispatchers.Main) {
                            loadingBar.visibility = View.GONE
                            Toast.makeText(
                                this@AddCustomersActivity,
                                "Failed to add customer: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            println(
                                "AddCustomer failed Server not responding: ${
                                    response.errorBody()?.string()
                                }"+"${response}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@AddCustomersActivity, "Customer Added", Toast.LENGTH_SHORT).show()
                    println("AddCustomer failed: ${e.message}")
                }
            }


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
    }
    private fun updateAutoCompleteTextView(categories: List<Categoryitem>) {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        this.autoCompleteTextView.setAdapter(adapter)
    }
}