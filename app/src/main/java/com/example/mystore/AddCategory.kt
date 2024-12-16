package com.example.mystore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

class AddCategory : AppCompatActivity() {

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
        setContentView(R.layout.activity_add_category)
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

        val categoryTxt = findViewById<EditText>(R.id.categoryTxt)
        val priceTxt = findViewById<EditText>(R.id.priceTxt)
        val addBtn = findViewById<AppCompatButton>(R.id.addBtn)
        val lodingBar = findViewById<ProgressBar>(R.id.lodingBar)

        addBtn.setOnClickListener {

            lodingBar.visibility = View.VISIBLE
            val name = categoryTxt.text.toString()
            val price = priceTxt.text.toString().toDoubleOrNull()?: 0.0


            val category = CategoryAdd(name, price)

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO){ apiServiceWithInterceptor.addCategory(category).execute()}
                    if (response.isSuccessful && response.body() != null) {
                        val categoryResponse = response.body()

                        if (categoryResponse?.success == true) {
                            // Customer added successfully
                            lodingBar.visibility = View.GONE
                            Toast.makeText(this@AddCategory, "New Category Added", Toast.LENGTH_SHORT)
                                .show()
                            // Optionally clear the input fields or navigate to another screen
                        } else {
                            // Handle error
                            lodingBar.visibility = View.GONE
                            val errorMessage = response.body()?.message ?: "Failed to add Category"
                            Toast.makeText(this@AddCategory, errorMessage, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        // Handle unsuccessful response
                        withContext(Dispatchers.Main) {
                            lodingBar.visibility = View.GONE
                            Toast.makeText(
                                this@AddCategory,
                                "Category not added: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            println(
                                "AddCategory failed Server not responding: ${
                                    response.errorBody()?.string()
                                }"+"${response}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Handle exception
                    lodingBar.visibility = View.GONE
                    Toast.makeText(this@AddCategory, "Category Added Failed", Toast.LENGTH_SHORT).show()
                    println("AddCategory failed: ${e.message}")
                }
            }


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
}