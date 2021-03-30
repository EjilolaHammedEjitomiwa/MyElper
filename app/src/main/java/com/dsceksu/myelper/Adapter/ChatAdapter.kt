package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ChatModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.ZoomImageActivity
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView

class ChatAdapter(val context: Context, val itemList: ArrayList<ChatModel>) : RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == 0) {
            LayoutInflater.from(context).inflate(R.layout.right_chat_design, parent, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.left_chat_design, parent, false)
        }

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        try {
            loadSenderImage(item.from!!, holder.profileImage)
            if (item.type == "image") {
                holder.image.visibility = View.VISIBLE
                holder.message.visibility = View.GONE
                Glide.with(context).load(item.message).into(holder.image)
            } else {
                holder.image.visibility = View.GONE
                holder.message.visibility = View.VISIBLE
                holder.message.text = item.message
            }
            holder.time.text = Utils.formatTime(item.time!!)
        } catch (e: Exception) {
        }

        holder.itemView.setOnClickListener {
            if (item.type == "image") {
                val intent = Intent(context, ZoomImageActivity::class.java)
                intent.putExtra("url", item.message)
                context.startActivity(intent)
            }
        }
    }


    private fun loadSenderImage(to: String, profileImage: CircularImageView) {
        Utils.database()
                .collection(Constants.users)
                .document(to)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val user = it.toObject(UsersModel::class.java)
                        Utils.loadImage(context, user!!.avatar, profileImage)
                    }
                }
    }

    override fun getItemViewType(position: Int): Int {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val item = itemList[position]
        return if (currentUser!!.uid == item.from) {
            0
        } else {
            1
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircularImageView = itemView.findViewById(R.id.profile_dp)
        val message: TextView = itemView.findViewById(R.id.message)
        val time: TextView = itemView.findViewById(R.id.time)
        val container: LinearLayout = itemView.findViewById(R.id.container)
        val image: ImageView = itemView.findViewById(R.id.image)
    }
}