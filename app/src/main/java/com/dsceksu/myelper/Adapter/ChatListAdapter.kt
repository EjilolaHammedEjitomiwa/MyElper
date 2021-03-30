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
import com.dsceksu.myelper.Models.ChatListModel
import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.mikhaellopez.circularimageview.CircularImageView

class ChatListAdapter(val context: Context, val userList: ArrayList<ChatListModel>) : RecyclerView.Adapter<ChatListAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.users_list_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        loadUserDetails(holder.username, holder.profileImage, user.userID)
        holder.message.text = user.message
        if (user.to == Utils.currentUserID()) {
            holder.message.setTextColor(context.resources.getColor(R.color.colorPrimary))
        } else {
            holder.message.setTextColor(context.resources.getColor(R.color.gray))
        }
        holder.time.text = Utils.formatTime(user.time)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Constants.recieverID, user.userID)
            context.startActivity(intent)
        }
    }

    private fun loadUserDetails(username: TextView, profileImage: CircularImageView, userId: String) {
        Utils.database().collection(Constants.users)
                .document(userId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val user = it.toObject(UsersModel::class.java)
                        Glide.with(context).load(user!!.avatar).into(profileImage)
                        username.text = user.username
                    }
                }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircularImageView = itemView.findViewById(R.id.user_list_design_profileImage)
        val username: TextView = itemView.findViewById(R.id.user_list_design_username)
        val message: TextView = itemView.findViewById(R.id.user_list_design_message)
        val time: TextView = itemView.findViewById(R.id.user_list_design_time)
        val online: TextView = itemView.findViewById(R.id.user_list_design_online)
    }
}