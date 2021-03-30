package com.dsceksu.myelper.Fragments
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.dsceksu.myelper.constants.Constants
import com.dsceksu.myelper.Models.*
import com.dsceksu.myelper.R
import com.dsceksu.myelper.activities.SubmitKYCActivity
import com.dsceksu.myelper.helper.Utils
import com.dsceksu.myelper.user_authentication.SigninActivity
import com.google.firebase.auth.FirebaseAuth
import com.jem.fliptabs.FlipTab
import com.theartofdev.edmodo.cropper.CropImage
import es.dmoral.toasty.Toasty

import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*

class AccountFragment : Fragment(){

    private var selectedFragment: Fragment? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_account, container, false)

        childFragmentManager.beginTransaction().disallowAddToBackStack().replace(R.id.account_fragment_frame,  BuyerAccountFragment()).commit()
        accountSwitch(view.account_fragment_flipTab)
        view.account_fragment_profileImage.setOnClickListener {
            cropImage()
        }
        view.account_activity_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Animatoo.animateFade(context)
        }

        loadUserInfo()
        isAccountActivated()

        view.account_fragment_activateAccount.setOnClickListener {
            if (view.account_fragment_activateAccount.text == "Pending Activation"){
                Toasty.info(requireContext(),"Your account is pending activation",Toasty.LENGTH_LONG).show()
            }else{
                startActivity(Intent(requireContext(),SubmitKYCActivity::class.java))
                Animatoo.animateFade(requireContext())
            }
        }

        return view
    }

    private fun accountSwitch(accountFragmentFliptab: FlipTab) {
        accountFragmentFliptab.setTabSelectedListener(object : FlipTab.TabSelectedListener {
            override fun onTabSelected(isLeftTab: Boolean, tabTextValue: String) {
                selectedFragment = when (isLeftTab) {
                    true -> {
                        BuyerAccountFragment()
                    }
                    false -> {
                        SellerAccountFragment()
                    }
                }
                if (selectedFragment != null) {
                    childFragmentManager.beginTransaction().disallowAddToBackStack().replace(R.id.account_fragment_frame, selectedFragment!!).commit()
                }
            }
            override fun onTabReselected(isLeftTab: Boolean, tabTextValue: String) {
            }
        })
    }


    private fun loadUserInfo() {
        Utils.database().collection(Constants.users)
                .document(Utils.currentUserID())
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        val user = it.toObject(UsersModel::class.java)
                        try {
                            account_fragment_username.text =  user!!.username
                        }catch (e:java.lang.Exception){}
                        try {
                            account_fragment_walletBalance.text = user!!.wallet_balance
                        }catch (e:java.lang.Exception){}
                        try {
                            account_fragment_adsSlotLeft.text = user!!.ads_slot_left.toString()
                        }catch (e:java.lang.Exception){}
                        if (user!!.avatar != ""){
                            try {
                                Glide.with(context!!).load(user.avatar).into(account_fragment_profileImage)
                            }catch (e:Exception){}
                        }
                        if (!user.activated!!){
                            try {
                                account_fragment_activateAccount.visibility = View.VISIBLE
                            }catch (e:java.lang.Exception){}
                        }
                        //set followers count
                        Utils.database().collection(Constants.users)
                                .document(Utils.currentUserID())
                                .collection(Constants.followers)
                                .get()
                                .addOnSuccessListener {followers ->
                                    if (!followers.isEmpty){
                                        try {
                                            account_fragment_followersCount.text = "${followers.documents.size} followers"
                                        }catch (e:java.lang.Exception){}

                                    }else{
                                        try {
                                            account_fragment_followersCount.text = "No followers"
                                        }catch (e:java.lang.Exception){}

                                    }
                                }
                    }
                }
    }
    private fun cropImage() {
        CropImage.activity().setAspectRatio(1, 1).start(activity!!)
    }
    private fun isAccountActivated(){
        Utils.database()
                .collection(Constants.kycInfo)
                .document(Utils.currentUserID())
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        if (it["status"] == "pending"){
                            try {
                                account_fragment_activateAccount.text = "Pending Activation"
                            }catch (e:Exception){}
                        }
                    }
                }
    }
}