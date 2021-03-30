package com.dsceksu.myelper.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.Models.bankLists.Banks
import com.dsceksu.myelper.activities.PaymentInformationActivity
import com.dsceksu.myelper.R

class BankVariationAdapter(val context: Context, val itemLists: Banks) : RecyclerView.Adapter<BankVariationAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.data_variation_design, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return itemLists.data!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemLists.data!![position]
        holder.bankName.text = item.name.toString()
        holder.itemView.setOnClickListener {
            if(context is PaymentInformationActivity){
                context.bankListRecyclerView!!.visibility = View.GONE
                context.bankCode = item.code.toString()
                context.bankName = item.name.toString()
                context.bankEditText!!.setText(item.name.toString())
            }
        }
    }
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankName: TextView = itemView.findViewById(R.id.bank_variation_design_bankName)
    }
}