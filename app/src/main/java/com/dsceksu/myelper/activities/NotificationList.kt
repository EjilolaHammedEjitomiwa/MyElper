package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.NotificationListAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.NotificationModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_notification_list.*

class NotificationList : AppCompatActivity() {
    private var notificationList = ArrayList<NotificationModel>()
    private var adapter: NotificationListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)

        adapter = NotificationListAdapter(this, notificationList)

        notification_list_recyclerView.setHasFixedSize(true)
        val wishListLayoutManager = LinearLayoutManager(this)
        wishListLayoutManager.reverseLayout = true
        wishListLayoutManager.stackFromEnd = true
        notification_list_recyclerView.layoutManager = wishListLayoutManager
        notification_list_recyclerView.adapter = adapter


        getNotificationLists()


    }

    private fun getNotificationLists() {
        Utils.database()
                .collection(Constants.notification)
                .whereEqualTo("userID", Utils.currentUserID())
                .orderBy("date")
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        notificationList.clear()
                        for (data in it.documents) {
                            val notification = data.toObject(NotificationModel::class.java)
                            notification!!.id = data.id
                            notificationList.add(notification)
                        }
                        adapter!!.notifyDataSetChanged()
                        Utils.dismissLoader()
                    } else {
                        Utils.dismissLoader()
                        Toasty.info(this, "No new notification",Toasty.LENGTH_LONG).show()
                    }
                }

    }

    override fun onResume() {
        super.onResume()
        //Utils.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
       // Utils.setUserOffline()
    }
}