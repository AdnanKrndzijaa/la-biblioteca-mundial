package com.example.thereadingroom

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.thereadingroom.databinding.ActivityAdminSettingsBinding
import com.example.thereadingroom.login.Login
import com.example.thereadingroom.user.Home
import com.example.thereadingroom.user.Library
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminSettings : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()

        binding.logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sign out")
                .setMessage("Do you really want to sign out?")
                .setPositiveButton("Continue") {a, d ->
                    firebaseAuth.signOut()
                    checkUser()
                }
                .setNegativeButton("Cancel") {a, d ->
                    a.dismiss()
                }
                .show()
        }
        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.profile.setOnClickListener {
            startActivity(Intent(this, MyProfile::class.java))
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = "${snapshot.child("first_name").value}"
                    val lastName = "${snapshot.child("last_name").value}"
                    val email = "${snapshot.child("email").value}"
                    val profilePicture = "${snapshot.child("profilePicture").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    val formattedDate = MyApp.formatTimeStampShort(timestamp.toLong())
                    binding.joined.text = "Joined " + formattedDate

                    binding.text1.setText("Logged in as " + firstName + " " + lastName)
                    binding.name.setText(firstName + " " + lastName)
                    binding.email.setText(email)

                    try {
                        Glide.with(this@AdminSettings)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.profilePicture)
                    } catch (e: Exception) {

                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        } else {

        }
    }
}