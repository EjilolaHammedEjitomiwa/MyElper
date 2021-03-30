package com.dsceksu.myelper.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.Models.GetStartedImageModel
import com.dsceksu.myelper.R
import com.smarteist.autoimageslider.SliderViewAdapter

class GetStartedAdapter(val context: Context, var itemList: ArrayList<GetStartedImageModel>) : SliderViewAdapter<GetStartedAdapter.SliderAdapterVH>() {
    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate: View = LayoutInflater.from(parent.context).inflate(R.layout.d_get_started_design, null)
        return SliderAdapterVH(inflate)
    }
    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem = itemList[position]
        viewHolder.image.scaleType = ImageView.ScaleType.CENTER_CROP
        try {
            Glide.with(context).load(sliderItem.image_url).into(viewHolder.image)
        }catch (e:Exception){}
    }

    override fun getCount(): Int {
        return itemList.size
    }

    inner class SliderAdapterVH(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.get_started_design_image)
    }

}