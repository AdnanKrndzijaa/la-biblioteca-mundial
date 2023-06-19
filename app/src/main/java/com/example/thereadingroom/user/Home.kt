package com.example.thereadingroom.user

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.thereadingroom.*
import com.example.thereadingroom.adminpannel.AdminDashboard
import com.example.thereadingroom.adminpannel.books.ModelPDF
import com.example.thereadingroom.databinding.ActivityHomeBinding
import com.example.thereadingroom.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Home : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    var listQuotes = mutableListOf(
        R.string.quote_string,
        R.string.quote_string2,
        R.string.quote_string3,
        R.string.quote_string4,
        R.string.quote_string5,
        R.string.quote_string6,
        R.string.quote_string7,
        R.string.quote_string8,
        R.string.quote_string9,
        R.string.quote_string10
    )
    var quoteNumber = 0
    var mainText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadUserInfo()

        quoteOnAppLoaded()

        binding.settings.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
            finish()
        }
        binding.libraryIcon.setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }

        binding.infoIcon.setOnClickListener {
            startActivity(Intent(this, Info::class.java))
            finish()
        }
        binding.libraryCard.setOnClickListener {
            startActivity(Intent(this, Library::class.java))
        }
        binding.infoCard.setOnClickListener {
            startActivity(Intent(this, Info::class.java))
            finish()
        }
        binding.websiteCard.setOnClickListener {
            Toast.makeText(this, "Website will be soon available!", Toast.LENGTH_SHORT).show()
        }

    }



    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {

        }
    }

    private fun quoteOnAppLoaded() {
        binding.quoteTitle.isEnabled = false
        quoteNumber = 0
        listQuotes.shuffle()
        typeText(getString(listQuotes[quoteNumber]))
        ++quoteNumber
    }

    private fun typeText(text: String) {
        mainText=""
        val textDelay = 50L

        GlobalScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            val updatedText = ""

            for (i in text.indices) {
                mainText = sb.append(updatedText + text[i]).toString()
                Thread.sleep(textDelay)
            }
        }
        val handler = Handler()
        Log.d("Main", "Handler started")
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                binding.quoteTitle.text = "$mainText|"
                handler.postDelayed(this,10)

                if (text == mainText) {
                    handler.removeCallbacks(this)
                    binding.quoteTitle.text = mainText
                }
            }
        }
        handler.postDelayed(runnable,0)
    }


    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = "${snapshot.child("first_name").value}"
                    val lastName = "${snapshot.child("last_name").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    binding.homeTitle.setText(firstName + "!")



                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}