package com.example.mystore

import com.google.gson.InstanceCreator
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.Request
import okio.Timeout
import java.lang.reflect.Type
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallInstanceCreator : InstanceCreator<Call<*>> {
    override fun createInstance(type: Type): Call<*> {
        // You can return a dummy or mock Call instance here
        return object : Call<CategoryResponse> {
            override fun enqueue(callback: Callback<CategoryResponse>) {
                // No-op
            }

            override fun isExecuted(): Boolean {
                return false
            }

            override fun cancel() {
                // No-op
            }

            override fun isCanceled(): Boolean {
                return false
            }

            override fun clone(): Call<CategoryResponse> {
                return this
            }

            override fun request(): okhttp3.Request {
                return Request.Builder().url("https://ice-factory-backend.onrender.com/api/v1/").build()
            }

            override fun timeout(): Timeout {
                TODO("Not yet implemented")
            }

            override fun execute(): Response<CategoryResponse> {
                throw UnsupportedOperationException("Not implemented")
            }
        }
    }

}