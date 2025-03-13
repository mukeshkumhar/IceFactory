package com.example.mystore

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections.addAll

class RecycleAdapterOrder(private var orders: List<Order>):

    RecyclerView.Adapter<RecycleAdapterOrder.CustomerViewHolder>()
    {

        var onItemClick: ((Order) -> Unit)? = null

        inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val email : TextView = itemView.findViewById(R.id.orderemailRecycle)
            val name : TextView = itemView.findViewById(R.id.ordernameRecycle)
            val category : TextView = itemView.findViewById(R.id.ordercategoryRecycle)
            val weight : TextView = itemView.findViewById(R.id.orderweightRecycle)
            val amount : TextView = itemView.findViewById(R.id.orderamountRecycle)
            val cardView: CardView = itemView.findViewById(R.id.cardViewBilling)


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.order_layout_recycleview, parent, false)
            return CustomerViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {

            val order = orders[position]
            holder.email.text = order.customer.email.toString()
            holder.name.text = order.customer.fullName
            holder.category.text = order.customer?.category?.name
            holder.weight.text = order.quantity.toString()
            holder.amount.text = order.value.toString()
            holder.cardView.setOnClickListener{
                println("Item Clicked: ${order.customer.fullName}")
                val context= holder.itemView.context
                println(context)
                if (context != null){
                    val intent = Intent(context, Billings::class.java)
                    intent.putExtra("email", order.customer.email)
                    println(order.customer.email)

                    context.startActivity(intent)


                    onItemClick?.invoke(orders[position])
                }

            }
        }

        override fun getItemCount(): Int {

            return orders.size
        }

        fun updateOrders(newOrders: List<Order>) {
            this.orders= newOrders
            notifyDataSetChanged()
        }
    }
