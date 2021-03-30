package com.dsceksu.myelper.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.dsceksu.myelper.Adapter.ChatListAdapter
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.ChatListModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.android.synthetic.main.fragment_chat_list.view.*

class ChatListFragment : Fragment() {
    private var userList = ArrayList<ChatListModel>()
    private var userAdapter: ChatListAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)

        userAdapter = ChatListAdapter(context!!, userList)
        view.chat_list_fragment_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        view.chat_list_fragment_recyclerView.layoutManager = layoutManager
        view.chat_list_fragment_recyclerView.adapter = userAdapter

        getUserLists()

        return view
    }

    private fun getUserLists() {
        Utils.database().collection(Constants.chatList)
                .document(Utils.currentUserID())
                .collection(Utils.currentUserID())
                .addSnapshotListener { value, error ->
                    if (!value!!.isEmpty){
                        userList.clear()
                        for (data in value.documents){
                            val user = data.toObject(ChatListModel::class.java)
                            userList.add(user!!)
                        }
                        userList.sortByDescending{p0 ->
                            p0.time
                        }
                        userAdapter!!.notifyDataSetChanged()
                        try {
                            chat_list_fragment_progress.visibility = View.GONE
                        }catch (e:Exception){}
                    }

                }
    }


}