package com.example.thereadingroom.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.thereadingroom.*
import com.example.thereadingroom.adminpannel.AdminDashboard
import com.example.thereadingroom.databinding.ActivityLoginBinding
import com.example.thereadingroom.forgotpassword.ForgotPassword
import com.example.thereadingroom.registration.Registration
import com.example.thereadingroom.user.Home
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.loginContinueButton.setOnClickListener {
            validateData()
        }
        binding.register.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
            finish()
        }
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
            finish()
        }
    }


    private fun validateData() {
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
        } else {
           loginUser()
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Checking user...")
        val firebaseUser = firebaseAuth.currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val accountType = snapshot.child("accountType").value

                    if (accountType == "user") {
                        startActivity(Intent(this@Login, Home::class.java))
                        finish()
                    } else if (accountType == "admin") {
                        startActivity(Intent(this@Login, AdminDashboard::class.java))
                        finish()
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}