package com.dsceksu.myelper.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.UserAdapter
import com.dsceksu.myelper.constants.Constants

import com.dsceksu.myelper.Models.UsersModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.fragment_users.view.*
import kotlinx.android.synthetic.main.fragment_users.view.all_product_fragment_search
import me.solidev.loadmore.AutoLoadMoreAdapter
import java.util.*
import kotlin.collections.ArrayList

class UsersFragment : Fragment() {
    private var userList = ArrayList<UsersModel>()
    private var userAdapter: UserAdapter? = null
    var searchText: String = ""
    private var autoLoader: AutoLoadMoreAdapter? = null
    private var lastDocumentSnapshot: DocumentSnapshot? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        userAdapter = UserAdapter(context!!, userList)
        autoLoader = AutoLoadMoreAdapter(context, userAdapter)
        autoLoader!!.setOnLoadListener(object : AutoLoadMoreAdapter.OnLoadListener {
            override fun onLoadMore() {
                getUserLists()
            }

            override fun onRetry() {
            }
        })

        view.user_fragment_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        view.user_fragment_recyclerView.layoutManager = layoutManager
        view.user_fragment_recyclerView.adapter = autoLoader

        getUserLists()

        view.all_product_fragment_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != "" && query != null) {
                    lastDocumentSnapshot = null
                    userList.clear()
                    autoLoader!!.notifyDataSetChanged()
                    searchText = query.toLowerCase(Locale.ROOT)
                    getUserLists()
                }

                return false
            }
        })
        view.all_product_fragment_search.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                lastDocumentSnapshot = null
                userList.clear()
                autoLoader!!.notifyDataSetChanged()
                searchText = ""
                getUserLists()

                return true
            }
        })
        return view
    }

    private fun getUserLists() {
        if (lastDocumentSnapshot == null) {
            try {
                user_fragment_progress.visibility = View.VISIBLE
            } catch (e: java.lang.Exception) {
            }
        }
        val query: Query = if (searchText == "") {
            if (lastDocumentSnapshot == null) {
                Utils.database().collection(Constants.users)
                        .limit(10)
            } else {
                Utils.database().collection(Constants.products)
                        .startAfter(lastDocumentSnapshot!!)
                        .limit(10)
            }
        } else {
            Utils.database().collection(Constants.users)
        }

        query.get().addOnSuccessListener {it1 ->
            //set last snapshot
            if(it1.size() > 0){
                lastDocumentSnapshot = it1.documents[it1.size()-1]
                for (snapshot in it1){
                    val users =  snapshot.toObject(UsersModel::class.java)
                    if (searchText =="") {
                        users.uid = snapshot.id
                        userList.add(users)
                    }
                    else {
                        if (users.username.toLowerCase(Locale.ROOT).contains(searchText)) {
                            users.uid = snapshot.id
                            userList.add(users)
                        }
                    }
                }

                userList.sortBy { it.username }
                autoLoader!!.notifyDataSetChanged()
                autoLoader!!.showLoadMore()
                try {
                    user_fragment_progress.visibility = View.GONE
                } catch (e: Exception) {
                }
            }
            else{
                Utils.dismissLoader()
                Toasty.error(requireContext(),"don't get any document", Toasty.LENGTH_LONG).show()
            }

        }

    }
}