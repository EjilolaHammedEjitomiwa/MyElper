package com.dsceksu.myelper.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dsceksu.myelper.*
import com.dsceksu.myelper.activities.*

import kotlinx.android.synthetic.main.fragment_buyer_account.view.*

class BuyerAccountFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_buyer_account, container, false)

        view.buyer_account_fragment_deliveryDetails.setOnClickListener {
            startActivity(Intent(context,
                    EditShippingAddress::class.java))
        }
        view.buyer_account_fragment_wishListContainer.setOnClickListener {
            startActivity(Intent(context,
                    WishListActivity::class.java))
        }

        view.buyer_account_fragment_myCart.setOnClickListener {
            startActivity(Intent(context,
                    CartActivity::class.java))
        }
        view.buyer_fragment_transactions.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionHistoryActivity::class.java))
        }
        view.buyer_account_fragment_myActiveOrders.setOnClickListener {
            val intent = Intent(context, OngoingOrderActivity::class.java)
            intent.putExtra("is_buyer", true)
            startActivity(intent)
        }
        view.buyer_account_fragment_completedOrders.setOnClickListener {
            val intent = Intent(context, CompletedOrderActivity::class.java)
            intent.putExtra("is_buyer", true)
            startActivity(intent)
        }

        view.buyer_account_fragment_favouriteSellers.setOnClickListener {
            startActivity(Intent(context,
                    FavouriteSellerActivity::class.java))
        }
        return view
    }


}
