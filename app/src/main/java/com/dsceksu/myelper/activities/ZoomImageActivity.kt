package com.dsceksu.myelper.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dsceksu.myelper.R
import com.dsceksu.myelper.helper.Utils
import kotlinx.android.synthetic.main.activity_zoom_image.*

class ZoomImageActivity : AppCompatActivity() {
    private var url:String? =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_image)

        url = intent.getStringExtra("url")
        zoom_image.with(url)
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
