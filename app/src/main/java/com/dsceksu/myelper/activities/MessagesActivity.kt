package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dsceksu.myelper.Fragments.ChatListFragment
import com.dsceksu.myelper.Fragments.UsersFragment
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.activity_messages.*

class MessagesActivity : AppCompatActivity() {
    private var selectedFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        supportFragmentManager.beginTransaction().replace(R.id.messages_activity_container, ChatListFragment()).commit()
        messages_activity_bottomBar.setActiveItem(0)
        bottomNavSetUp()
    }

    private fun bottomNavSetUp() {
        messages_activity_bottomBar.onItemSelected = {
            when (it) {
                0 -> selectedFragment = ChatListFragment()
                1 -> selectedFragment = UsersFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.messages_activity_container, selectedFragment!!).commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        Utils.setUserOffline()
    }
}