package com.dsceksu.myelper.Fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dsceksu.myelper.*
import com.dsceksu.myelper.activities.*

import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.fragment_seller_account.view.*

class SellerAccountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_seller_account, container, false)
        view.seller_fragment_addNewProduct.setOnClickListener {
            startActivity(Intent(context, AddNewService::class.java))
        }


        view.seller_fragment_transactions.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionHistoryActivity::class.java))
        }

        view.seller_fragment_ongoingSalesContainer.setOnClickListener {
            val intent = Intent(context, OngoingOrderActivity::class.java)
            intent.putExtra("is_seller", true)
            startActivity(intent)
        }
        view.seller_fragment_paymentInformation.setOnClickListener {
            startActivity(Intent(context,
                    PaymentInformationActivity::class.java))
        }

        view.seller_fragment_completedSales.setOnClickListener {
            val intent = Intent(context, CompletedOrderActivity::class.java)
            intent.putExtra("is_seller", true)
            startActivity(intent)
        }

        view.seller_fragment_myService.setOnClickListener {
            val intent = Intent(context, MyServiceActivity::class.java)
            intent.putExtra("id", Utils.currentUserID())
            startActivity(intent)
        }
        return view
    }
}
