package com.example.mystore

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class RecycleAdapterSells(private var customers: List<Customer>) :
    RecyclerView.Adapter<RecycleAdapterSells.CustomerViewHolder>() {

    var onItemClick: ((Customer) -> Unit)? = null

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullNameTextView: TextView = itemView.findViewById(R.id.recycleFullname)
        val emailTextView: TextView = itemView.findViewById(R.id.recycleEmail)
        val cardView: CardView = itemView.findViewById(R.id.cardViewRecycle)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.customer_layout_recycleview, parent, false)
        return CustomerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {


        val customer = customers[position]
        holder.fullNameTextView.text = customer.fullName
        holder.emailTextView.text = customer.email
        holder.cardView.setOnClickListener {
            println("Item Clicked: ${customer.fullName}")
            val context= holder.itemView.context
            val intent = Intent(context, CustomerData::class.java)
            intent.putExtra("email", customer.email)
            context.startActivity(intent)


            onItemClick?.invoke(customers[position])
        }
    }

    override fun getItemCount(): Int {

        return customers.size
    }

    fun setFilteredList(customers: List<Customer>) {

        this.customers = customers
        notifyDataSetChanged()

    }

    fun updateOrders(newOrders: List<Customer>) {
        this.customers = newOrders
        notifyDataSetChanged()
    }


}