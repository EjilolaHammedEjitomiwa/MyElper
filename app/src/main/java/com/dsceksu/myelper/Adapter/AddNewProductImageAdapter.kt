package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.ZoomImageActivity

class AddNewProductImageAdapter (val context: Context, val imageLists: ArrayList<String>):
        RecyclerView.Adapter<AddNewProductImageAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.d_add_new_product_image,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageLists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image  = imageLists[position]
        try {
            Glide.with(context).load(image).into(holder.image)
        }catch (e:Exception){}

        holder.deleteIcon.setOnClickListener {
            imageLists.removeAt(position)
            notifyDataSetChanged()
        }
        holder.image.setOnClickListener {
            val intent = Intent(context, ZoomImageActivity::class.java)
            intent.putExtra("url", image)
            context.startActivity(intent)
        }
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.add_new_product_design_image)
        val deleteIcon:ImageView = itemView.findViewById(R.id.add_new_product_design_iconDelete)
    }
}