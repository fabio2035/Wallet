package com.example.wallet.ui


import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

import com.example.wallet.R

class PrivacyPolicy : AppCompatActivity() {

    internal lateinit var web: WebView
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)

        /*toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.setLogo(R.drawable.logo_1_main)
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayUseLogoEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)*/

        web = findViewById(R.id.webView) as WebView
        web.loadUrl("file:///android_asset/PrivacyPolicy.html")
    }
}