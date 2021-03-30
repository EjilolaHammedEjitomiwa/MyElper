package com.dsceksu.myelper.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsceksu.myelper.activities.ChatActivity
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.mikhaellopez.circularimageview.CircularImageView
import java.lang.Exception

class UserAdapter (val context: Context, val userList: ArrayList<UsersModel>) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.users_list_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        try {
            Glide.with(context).load(user.avatar).into(holder.profileImage)
            holder.username.text = user.username
        } catch (e: Exception) { }

        if(user.presence == Constants.online){
            holder.online.text = "online"
            holder.online.setTextColor(context.resources.getColor(R.color.green))
        }else{
            holder.online.text = Utils.formatTime(user.presence.toLong())
            holder.online.setTextColor(context.resources.getColor(R.color.gray))
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Constants.recieverID, user.uid)
            context.startActivity(intent)
        }
    }
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircularImageView = itemView.findViewById(R.id.user_list_design_profileImage)
        val username: TextView = itemView.findViewById(R.id.user_list_design_username)
        val online:TextView = itemView.findViewById(R.id.user_list_design_online)
    }
}