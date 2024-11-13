package com.example.mystore
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("user/register")
    fun registerUser(@Body user: RegisterUser): Call<RegisterResponse>

    @POST("user/login")
    fun loginUser(@Body user: LoginUser): Call<LoginResponse>


    @POST("all/add-new-customer")
    fun addCustomer(@Body customer: CustomerAdd): Call<CustomerResponse>


    @POST("all/add-category")
     fun addCategory(@Body category: CategoryAdd): Call<CategoryResponse>

    @POST("all/create-order")
    fun createOrder(@Body order: CustomerDataWeight): Call<CustomerDataWeightResponce>

    @POST("all/get-order-history") // Replace with your actual endpoint
    suspend fun getOrderHistory(@Body emailRequest: EmailRequest): Response<OrderHistoryResponce>



    @GET("all/get-all-customer") // Replace with your actual endpoint
    suspend fun getCustomers(): Response<CustomerResponseName>

    @GET("all/get-all-category")
    suspend fun getCategoryItem(): Response<CategoryitemResponce>



    @GET("all/get-all-order")
    suspend fun getOrders(): Response<OrderResponce>





}