package com.example.thereadingroom.adminpannel

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.thereadingroom.databinding.ActivityAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCategory : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.back.setOnClickListener {
            onBackPressed()
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.submit.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        category = binding.category.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter a book category", Toast.LENGTH_SHORT).show()
        } else {
            addCategory()
        }

    }

    private fun addCategory() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] ="${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Category successfully added", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboard::class.java))
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Adding category failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}