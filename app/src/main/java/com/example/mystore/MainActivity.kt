package com.example.mystore

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory


class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("AccessToken", null)

        if (accessToken != null) {
            // User is already logged in, proceed to main screen
            Toast.makeText(this, "You logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Optional: Finish the launcher activity
        } else {
            // User is not logged in, show login screen
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this, MainActivity::class.java))
//            finish() // Optional: Finish the launcher activity
        }




            val warningTxt = findViewById<TextView>(R.id.errorTxt)
            val loginBtn = findViewById<AppCompatButton>(R.id.loginButton)
            val Email = findViewById<EditText>(R.id.loginEmail)
            val Password = findViewById<EditText>(R.id.loginPassword)

            val registerPage = findViewById<AppCompatButton>(R.id.registerPagebtn)

            registerPage.setOnClickListener {
                val registerpage = Intent(this, RegisterPage::class.java)
                startActivity(registerpage)
                finish()
            }
//            val apiService = retrofit.create(ApiService::class.java)
//
//            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userRepository = UserRepository(apiService, sharedPreferences)

            loginBtn.setOnClickListener {
                val email = Email.text.toString()
                val password = Password.text.toString()

                // Create a User object with the provided email and password
                val user = LoginUser(email, password)

                // Call the loginUser API asynchronously
                lifecycleScope.launch {

                    val result = userRepository.login(LoginUser(email, password))


                    try {
                        val response = withContext(Dispatchers.IO) { apiService.loginUser(user).execute() }

                        if (response.isSuccessful && response.body() != null) {


                            val loginResponse = response.body()

                            println("Login responce: $response")

                            if (loginResponse?.success == true) {
                                // Handle successful login (e.g., save token, navigate to home page)
                                val accessToken = response.body()?.data?.tokens?.AccessToken
                                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("AccessToken", accessToken)
                                editor.apply()
                                println("Access Token : $accessToken")
                                // Save the token securely (e.g., using SharedPreferences)
//                        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
//                            val editor = sharedPreferences.edit()
//                            editor.putString("authToken", LoginResponse.token)
//                            editor.apply()
                                Toast.makeText(
                                    this@MainActivity,
                                    loginResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()  // Prevent going back to LoginActivity

                                // Navigate to the home activity
                            } else {
                                // Handle login failure (e.g., display an error message)
                                Toast.makeText(
                                    this@MainActivity,
                                    "Wrong email or Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println("Login failed: " + "${loginResponse?.message}" + "${loginResponse?.success}")

                            }
                        } else {
                            // Handle unsuccessful response
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Login failed: ${response.message()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                println("Login failed else: ${response.errorBody()?.string()}")
                            }
                        }
                    } catch (e: Exception) {
                        // Handle network errors or other exceptions
                        Toast.makeText(
                            this@MainActivity,
                            "Login failed" + "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        println("Login failed catch: " + "${e.message}")
                    }
                }
            }


//        loginBtn.setOnClickListener{
//            val email = findViewById<EditText>(R.id.loginEmail).text.toString()
//            val password = findViewById<EditText>(R.id.loginPassword).text.toString()
//            Toast.makeText(this@MainActivity, "Login.....", Toast.LENGTH_SHORT).show()
//
//            val loginUser = User(0,email, password)
//
//            RetrofitInstance.api.loginUser(loginUser).enqueue(object : retrofit2.Callback<LoginResponse> {
//                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(this@MainActivity, "Login.....", Toast.LENGTH_SHORT).show()
//                        val loginResponse = response.body()
//                        if (loginResponse?.success == true) {
//                            Toast.makeText(this@MainActivity, "Login...", Toast.LENGTH_SHORT).show()
//                            // Login successful, save the token and redirect to HomeActivity
//                            val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
//                            val editor = sharedPreferences.edit()
//                            editor.putString("authToken", loginResponse.token)
//                            editor.apply()
//
//                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
//                            startActivity(intent)
//                            finish()  // Prevent going back to LoginActivity
//                        } else {
//                            // Incorrect email or password
//                            Toast.makeText(this@MainActivity, loginResponse?.message, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
//                    Toast.makeText(this@MainActivity, "Login failed", Toast.LENGTH_SHORT).show()
//                }
//            })
//
//
//        }

//        var loginConst = 0
//
//        if (loginConst == 1) {
//
//            Toast.makeText(this, "You logged in", Toast.LENGTH_SHORT).show()
//            val homePage = Intent(this, HomeActivity::class.java)
//            startActivity(homePage)
//            finish()
//        }
//            loginBtn.setOnClickListener{
//
//                if(loginEmail.text.toString() == "testing@gmail.com" && loginPassword.text.toString() == "testing"){
//                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
//                    loginConst == 1
//                    val homePage = Intent(this, HomeActivity::class.java)
//                    startActivity(homePage)
//                    finish()
//
//                } else {
//                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
//                    warningTxt.visibility = View.VISIBLE
//                }
//
//
//            }


        }



}