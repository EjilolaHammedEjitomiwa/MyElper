package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsceksu.myelper.Adapter.TransactionHistoryAdapter
import com.dsceksu.myelper.Models.TransactionHistoryModel
import com.dsceksu.myelper.R
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.helper.Utils
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_transaction_history.*
import me.solidev.loadmore.AutoLoadMoreAdapter
import java.lang.Exception

class TransactionHistoryActivity : AppCompatActivity() {
    private var transactionLists = ArrayList<TransactionHistoryModel>()
    private var transactionAdapter: TransactionHistoryAdapter? = null
    private var autoLoader: AutoLoadMoreAdapter? = null
    private var lastDocumentSnapshot: DocumentSnapshot? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        transactionAdapter = TransactionHistoryAdapter(this, transactionLists)
        autoLoader = AutoLoadMoreAdapter(this, transactionAdapter)
        autoLoader!!.setOnLoadListener(object : AutoLoadMoreAdapter.OnLoadListener {
            override fun onLoadMore() {
                getTransactionLists()
            }
            override fun onRetry() {
            }
        })

        transaction_history_recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        transaction_history_recyclerView.layoutManager = layoutManager
        transaction_history_recyclerView.adapter = autoLoader
        Utils.showLoader(this, "Loading transactions....")
        getTransactionLists()

        transaction_history_backIcon.setOnClickListener{
            finishAndRemoveTask()
        }
    }

    private fun getTransactionLists() {
        val query = if (lastDocumentSnapshot == null) {
            Utils.database().collection(Constants.transactions)
                    .whereEqualTo(Constants.userID, Utils.currentUserID())
                    .orderBy(Constants.date, Query.Direction.DESCENDING)
                    .limit(10)
        } else {
            Utils.database().collection(Constants.transactions)
                    .whereEqualTo(Constants.userID, Utils.currentUserID())
                    .orderBy(Constants.date,Query.Direction.DESCENDING)
                    .startAfter(lastDocumentSnapshot!!)
                    .limit(10)
        }

        query.get()
                .addOnSuccessListener { it1 ->
                    if (it1.size() > 0) {
                        lastDocumentSnapshot = it1.documents[it1.size() - 1]
                        for (snapshot in it1) {
                            val transaction = snapshot.toObject(TransactionHistoryModel::class.java)
                            transactionLists.add(transaction)
                        }

                        if (transactionLists.isEmpty()) {
                            Toasty.info(this, "No transaction found", Toasty.LENGTH_LONG).show()
                        }
                        autoLoader!!.notifyDataSetChanged()
                        Utils.dismissLoader()
                        autoLoader!!.showLoadMore()

                    } else {
                        Utils.dismissLoader()
                        try {
                            Toasty.info(this, "No transaction found", Toasty.LENGTH_LONG).show()
                        } catch (e: Exception) {
                        }
                    }
                }
                .addOnFailureListener {
                    Utils.dismissLoader()
                    Toasty.error(this, "${it.message}", Toasty.LENGTH_LONG).show()
                }


    }
}