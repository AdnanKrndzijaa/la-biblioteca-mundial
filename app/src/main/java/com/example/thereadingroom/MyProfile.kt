package com.example.thereadingroom

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.thereadingroom.databinding.ActivityMyProfileBinding
import com.example.thereadingroom.login.Login
import com.example.thereadingroom.user.Home
import com.example.thereadingroom.user.Library
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyProfile : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var progressDialog: ProgressDialog

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
    var mainText2 = ""
    var mainText3 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileEmail.text = "N/A"
        binding.joiningDate.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait ...")
        progressDialog.setCanceledOnTouchOutside(false)

        quoteOnAppLoaded()
        quoteOnAppLoaded2()
        quoteOnAppLoaded3()
        loadUserInfo()

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }


        binding.verificationStatus.setOnClickListener {
            if (firebaseUser.isEmailVerified) {
                Toast.makeText(this, "Already verified!", Toast.LENGTH_SHORT).show()
            } else {
                emailVerificationDialog()
            }
        }
    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Email Verification")
            .setMessage("Are you sure you want to send email verification instructions to ${firebaseUser.email}")
            .setPositiveButton("Send") { d,e->
                sendVerificationLink()
            }
            .setNegativeButton("Cancel") {d,e->
                d.dismiss()
            }
            .show()
    }

    private fun sendVerificationLink() {
        progressDialog.setMessage("Sending a verification link to ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Verification link successfully sent to ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Sending failed due to ${e.message}", Toast.LENGTH_SHORT).show()
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
        binding.q1.isEnabled = false
        quoteNumber = 0
        listQuotes.shuffle()
        typeText(getString(listQuotes[quoteNumber]))
        ++quoteNumber
    }

    private fun typeText(text: String) {
        mainText= ""
        val textDelay = 0L

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
                binding.q1.text = "$mainText|"
                handler.postDelayed(this,0)

                if (text == mainText) {
                    handler.removeCallbacks(this)
                    binding.q1.text = mainText
                }
            }
        }
        handler.postDelayed(runnable,0)
    }

    private fun quoteOnAppLoaded2() {
        binding.q2.isEnabled = false
        quoteNumber = 0
        listQuotes.shuffle()
        typeText2(getString(listQuotes[quoteNumber]))
        ++quoteNumber
    }

    private fun typeText2(text: String) {
        mainText2= ""
        val textDelay = 0L

        GlobalScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            val updatedText2 = ""

            for (i in text.indices) {
                mainText2 = sb.append(updatedText2 + text[i]).toString()
                Thread.sleep(textDelay)
            }
        }
        val handler = Handler()
        Log.d("Main", "Handler started")
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                binding.q2.text = "$mainText2|"
                handler.postDelayed(this,0)

                if (mainText2 != mainText) {
                    if (text == mainText2) {
                        handler.removeCallbacks(this)
                        binding.q2.text = mainText2
                    }
                }
            }
        }
        handler.postDelayed(runnable,0)
    }

    private fun quoteOnAppLoaded3() {
        binding.q3.isEnabled = false
        quoteNumber = 0
        listQuotes.shuffle()
        typeText3(getString(listQuotes[quoteNumber]))
        ++quoteNumber
    }

    private fun typeText3(text: String) {
        mainText3= ""
        val textDelay = 0L

        GlobalScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            val updatedText3 = ""

            for (i in text.indices) {
                mainText3 = sb.append(updatedText3 + text[i]).toString()
                Thread.sleep(textDelay)
            }
        }
        val handler = Handler()
        Log.d("Main", "Handler started")
        val runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                binding.q3.text = "$mainText3|"
                handler.postDelayed(this,0)

                if (mainText3 != mainText2 && mainText3 != mainText) {
                    if (text == mainText3) {
                        handler.removeCallbacks(this)
                        binding.q3.text = mainText3
                    }
                }

            }
        }
        handler.postDelayed(runnable,0)
    }

    private fun loadUserInfo() {
        if (firebaseUser.isEmailVerified) {
            binding.verificationStatus.setBackgroundResource(R.drawable.verified_account)
        } else {
            binding.verificationStatus.setBackgroundResource(R.drawable.unverified_account)
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("first_name").value}" + " " + "${snapshot.child("last_name").value}"
                    val nickname = "@"+"${snapshot.child("first_name").value}" + "${snapshot.child("last_name").value}"

                    val profilePicture = "${snapshot.child("profilePicture").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val accountType = "${snapshot.child("accountType").value}"
                    val uid = "${snapshot.child("uid").value}"

                    val formattedDate = MyApp.formatTimeStampShort(timestamp.toLong())
                    binding.profileName.text = name
                    binding.name1.text = name
                    binding.name2.text = name
                    binding.name3.text = name
                    binding.nickname.text = nickname
                    binding.nick1.text = nickname
                    binding.nick2.text = nickname
                    binding.nick3.text = nickname
                    binding.profileEmail.text = "  " + email
                    binding.joiningDate.text = "  Joined "+ formattedDate

                    try {
                        Glide.with(this@MyProfile)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.profilePicture)
                    } catch (e: Exception) {

                    }

                    try {
                        Glide.with(this@MyProfile)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.pic1)
                    } catch (e: Exception) {

                    }

                    try {
                        Glide.with(this@MyProfile)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.pic2)
                    } catch (e: Exception) {

                    }

                    try {
                        Glide.with(this@MyProfile)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.pic3)
                    } catch (e: Exception) {

                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}