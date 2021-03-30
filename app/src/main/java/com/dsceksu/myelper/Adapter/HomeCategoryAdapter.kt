package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.CategoryModel
import com.dsceksu.myelper.activities.CategoryServiceActivity
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils

class HomeCategoryAdapter(val context: Context, val categoryLists: ArrayList<CategoryModel>) : RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_home_category_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoryLists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryLists[position]
        try {
            Utils.loadImage(context,category.img_url,holder.image)
            holder.title.text = category.title
        } catch (e: Exception) { }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, CategoryServiceActivity::class.java)
            intent.putExtra(Constants.category, category.title)
            context.startActivity(intent)
        }
    }
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.home_category_design_image)
        val title: TextView = itemView.findViewById(R.id.home_category_design_title)
    }
}