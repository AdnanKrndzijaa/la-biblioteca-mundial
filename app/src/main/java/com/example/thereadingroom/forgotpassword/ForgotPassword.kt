package com.example.thereadingroom.forgotpassword

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.thereadingroom.databinding.ActivityForgotPasswordBinding
import com.example.thereadingroom.login.Login
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var email = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.forgotPasswordContinueButton.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        email = binding.email.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
        } else {
            resetPassword()
        }
    }

    private fun resetPassword() {
        progressDialog.setMessage("Resetting password...")
        progressDialog.show()
         firebaseAuth.sendPasswordResetEmail(email)
             .addOnSuccessListener {
                 progressDialog.dismiss()
                 Toast.makeText(this, "Reset password email sent", Toast.LENGTH_SHORT).show()
                 startActivity(Intent(this@ForgotPassword, Login::class.java))
                 finish()
             }
             .addOnFailureListener { e->
                 progressDialog.dismiss()
                 Toast.makeText(this, "Resetting failed due to ${e.message}", Toast.LENGTH_SHORT).show()
             }
    }
}