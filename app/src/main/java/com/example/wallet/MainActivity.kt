package com.example.wallet

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.wallet.databinding.ActivityMainBinding
import com.example.wallet.ui.PrivacyPolicy
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding : ActivityMainBinding

    //FireBase Authentication variables
    lateinit var mFirebaseAuth : FirebaseAuth
    lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener
    var RC_SIGN_IN : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()


        mAuthStateListener = object: FirebaseAuth.AuthStateListener {

            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

                var user = firebaseAuth.currentUser

                if(user != null){
                    //user is signed in
                    Toast.makeText(this@MainActivity, "You're now signed in. Welcome!", Toast.LENGTH_LONG).show()
                    onSignedInInitialize(user)
                }else{
                    //user is signed out
                    onSignedOutCleanup()
                    // Choose authentication providers
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo_1_main)
                            .setTheme(R.style.FirebaseUITheme)
                            .build(),
                        RC_SIGN_IN)
                }
            }

        }

    }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            if(requestCode == RC_SIGN_IN){
                if(resultCode == Activity.RESULT_OK){
                Toast.makeText(this@MainActivity, "Signed In!", Toast.LENGTH_LONG).show()
                }else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this@MainActivity, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
                }
            }
        }

    private fun onSignedOutCleanup() {
        //detach listeners cleanup
    }

    private fun onSignedInInitialize(user: FirebaseUser) {
        //functionality for when user is signed in
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_signOut ->
                //Sign Out
                {AuthUI.getInstance().signOut(this)
                true}
            R.id.action_disclaimer ->
                {OpenPrivacyPolicy()
                true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun OpenPrivacyPolicy() {
        // Opens the privacy policy activity
        val intent = Intent(
            applicationContext,
            PrivacyPolicy::class.java
        )
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onPause(){
        super.onPause()
        if(mAuthStateListener != null){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
        }
    }

    override fun onResume(){
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }
}
