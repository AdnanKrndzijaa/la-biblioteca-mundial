package com.example.thereadingroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.thereadingroom.adminpannel.AdminDashboard
import com.example.thereadingroom.user.Home
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashScreen : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable{
            checkUser()
        }, 1000)
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, Main::class.java))
            finish()
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val accountType = snapshot.child("accountType").value

                        if (accountType == "user") {
                            startActivity(Intent(this@SplashScreen, Home::class.java))
                            finish()
                        } else if (accountType == "admin") {
                            startActivity(Intent(this@SplashScreen, AdminDashboard::class.java))
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}