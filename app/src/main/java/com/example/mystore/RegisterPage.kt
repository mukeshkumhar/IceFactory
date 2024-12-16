package com.example.mystore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory


class RegisterPage : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(ApiService::class.java)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        val accessToken = sharedPreferences.getString("access_token", null)
//
//        if (accessToken != null) {
//            // User is already logged in, proceed to main screen
//            Toast.makeText(this, "You logged in", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, HomeActivity::class.java))
//            finish() // Optional: Finish the launcher activity
//        } else {
//            // User is not logged in, show login screen
//            Toast.makeText(this,"You are not logged in", Toast.LENGTH_SHORT).show()
////            startActivity(Intent(this, MainActivity::class.java))
////            finish() // Optional: Finish the launcher activity
//        }


            val loginPageBtn = findViewById<AppCompatButton>(R.id.loginPagebtn)
            val register = findViewById<AppCompatButton>(R.id.registerButton)
            val warningTxt = findViewById<TextView>(R.id.errorTxt)

            val name = findViewById<EditText>(R.id.registerName)
            val email = findViewById<EditText>(R.id.registerEmail)
            val password = findViewById<EditText>(R.id.registerPassword)
            val loadingBar = findViewById<ProgressBar>(R.id.lodingBar)

            register.setOnClickListener {
                loadingBar.visibility = View.VISIBLE

                val Name = name.text.toString()
                val Email = email.text.toString()
                val Password = password.text.toString()


                // Create a User object with the provided name, email, and password
                Log.d("RegisterPage", "Name: $Name, Email: $Email, Password: $Password")
                val user = RegisterUser(Name, Email, Password)


                // Call the registerUser API asynchronously
                lifecycleScope.launch {
                    try {
                        val response =
                            withContext(Dispatchers.IO) { apiService.registerUser(user).execute() }

                        if (response.isSuccessful && response.body() != null) {

                            val registerResponse = response.body()

                            println("Register Responce: $response")

                            if (registerResponse?.success == true) {
                                // Handle successful registration (e.g., display a success message, navigate to login page)
                                loadingBar.visibility = View.GONE
                                Toast.makeText(
                                    this@RegisterPage,
                                    registerResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate to the login activity
                                val intent = Intent(this@RegisterPage, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                // Handle registration failure (e.g., display an error message)
                                loadingBar.visibility = View.GONE
                                Toast.makeText(
                                    this@RegisterPage,
                                    "Wrong email or Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println("Register failed: " + "${registerResponse?.message}")
                            }
                        } else {
                            // Handle unsuccessful response
                            withContext(Dispatchers.Main) {
                                loadingBar.visibility = View.GONE
                                Toast.makeText(
                                    this@RegisterPage,
                                    "Register failed: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println(
                                    "Register failed Server not responding: ${
                                        response.errorBody()?.string()
                                    }"+"${response}"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Handle network errors or other exceptions
                        loadingBar.visibility = View.GONE
                        Toast.makeText(this@RegisterPage, "Register failed", Toast.LENGTH_SHORT)
                            .show()
                        println("Register failed: try " + "${e.message}")

                    }
                }

            }

            loginPageBtn.setOnClickListener {
                val loginPage = Intent(this, MainActivity::class.java)
                startActivity(loginPage)
            }
        }

}