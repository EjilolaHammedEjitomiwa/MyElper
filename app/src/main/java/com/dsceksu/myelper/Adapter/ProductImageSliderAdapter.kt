package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.R
import com.dsceksu.myelper.Models.AddNewProductImageModel
import com.dsceksu.myelper.activities.ZoomImageActivity
import com.smarteist.autoimageslider.SliderViewAdapter

class ProductImageSliderAdapter(val context: Context, var imageList: ArrayList<AddNewProductImageModel>) : SliderViewAdapter<ProductImageSliderAdapter.SliderAdapterVH>() {
    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate: View = LayoutInflater.from(parent.context).inflate(R.layout.d_product_details_image_slider, null)
        return SliderAdapterVH(inflate)
    }
    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val image = imageList[position]
        try {
            Glide.with(context).load(image.url).into(viewHolder.image)
        }catch (e:Exception){}
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context,
                ZoomImageActivity::class.java)
            intent.putExtra("url",image.url)
            context.startActivity(intent)
        }
    }

    override fun getCount(): Int {
        return imageList.size
    }

    inner class SliderAdapterVH(itemView: View) : ViewHolder(itemView) {
            var image: ImageView = itemView.findViewById(R.id.product_design_image)
    }

}