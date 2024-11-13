//package com.example.mystore
//
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.RecyclerView
//
//class RecycleAdapterBilling (private var customers: List<Order>):
//RecyclerView.Adapter<RecycleAdapterBilling.ViewHolder>(){
//
////    var onItemClick: ((Order) -> Unit)? = null
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val customerNameTextView: TextView = itemView.findViewById(R.id.recycleFullname)
//        val customerEmailTextView: TextView = itemView.findViewById(R.id.recycleEmail)
//        val cardView: CardView = itemView.findViewById(R.id.cardViewRecycle)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.customer_layout_recycleview, parent, false) // Assuming you have a layout for each customer item
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val customer = customers[position]
//        holder.customerNameTextView.text = customer.customer.fullName
//        holder.customerEmailTextView.text = customer.customer.email
//        holder.cardView.setOnClickListener {
//            println("Item Clicked: ${customer.customer.fullName}")
////            onCustomerClick(customer.customer.email)
////
//////            onItemClick?.invoke(customers[position])
////            onItemClick?.invoke(customers[position])
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return minOf(2,customers.size)
//    }
//
//    fun updateOrders(newOrders: List<Order>) {
//        this.customers= newOrders
//        notifyDataSetChanged()
//    }
//}