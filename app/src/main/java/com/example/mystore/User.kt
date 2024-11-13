package com.example.mystore

import java.util.Date

data class User(
    val _id: String,
    val fullName: String,
    val email: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String,
)

// Model for Registration
data class RegisterUser(
    val fullName: String,
    val email: String,
    val password: String
)

// Model for Login
data class LoginUser(
    val email: String,
    val password: String
)
data class LoginResponseData(
    val user: User,
    val tokens: AccessToken,

)

data class AccessToken(
    val AccessToken: String,
    val RefreshToken: String,

)

data class RegisterResponseData(
    val user: User,
    val RefreshToken: String,
    val AccessToken: String
)
data class RegisterResponse(
    val success: Boolean,
    val message: String,
)


data class LoginResponse(
    val statusCode: Int,
    val data: LoginResponseData,
    val AccessToken: String,  // Token in case of success
    val success: Boolean,
    val message: String
)

data class HomeResponse(
    val success: Boolean,
    val message: String,
    val user: User
)





data class CustomerAdd(
    val name: String,
    val email: String,
    val category: String
)

data class CustomerResponse(
    val message: String,
    val success: Boolean,
)




data class CategoryAdd(
    val name: String,
    val price: Double
)

data class CategoryResponse(
    val message: String,
    val success: Boolean,

)


data class Customer(
    val _id: String,
    val fullName: String,
    val email: String,
    val category: Category,
    val allOrders: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,

)

data class Billing(
    val _id: String,
    val fullName: String,
    val email: String,
    val category: Category,
    val allOrders: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,

    )

data class Category(
    val _id: String,
    val name: String,
    val price: Int,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CustomerResponseName(

    val statusCode: Int,
    val data: List<Customer>,
    val message: String,
    val success: Boolean
)




data class CustomerDataWeight(
    val email: String,
    val quantity: Double,
    val customDate: Date,

)
data class CustomerDataWeightResponce(
    val success: Boolean,
    val message: String,
    )




data class Order(
    val _id: String,
    val customer: CustomerDetails,
    val quantity: Double,
    val value: Double,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
)

data class CustomerDetails(
    val _id: String,
    val fullName: String,
    val email: String,
    val category: CustomerCategory,
    val allOrders: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CustomerCategory (
    val _id: String,
    val name: String,
    val price: Int,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,

)

data class OrderResponce(
    val statusCode: Int,
    val data: List<Order>,
    val message: String,
    val success: Boolean
)


data class Categoryitem(
    val _id: String,
    val name: String,
    val price: Int,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CategoryitemResponce(
    val statusCode: Int,
    val data: List<Categoryitem>
)



data class EmailRequest(val email: String)





data class OrderHistoryResponce (
    val statusCode: Int,
    val data: OrderHistory,
    val message: String,
    val success: Boolean,
)

data class OrderHistory(
    val _id: String,
    val fullName: String,
    val email: String,
    val category: String,
    val allOrders: List<OrderValue>,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
)

data class OrderValue(
    val _id: String,
    val customer: String,
    val quantity: Double,
    val value: Double,
    val customDate: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
)