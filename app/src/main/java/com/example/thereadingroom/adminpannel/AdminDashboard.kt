package com.example.thereadingroom.adminpannel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.thereadingroom.AdminSettings
import com.example.thereadingroom.R
import com.example.thereadingroom.Settings
import com.example.thereadingroom.databinding.ActivityAdminDashboardBinding
import com.example.thereadingroom.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboard : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        binding.searchBar.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.settings.setOnClickListener {
            startActivity(Intent(this, AdminSettings::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
        }

        binding.addCategory.setOnClickListener {
            startActivity(Intent(this, AddCategory::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
        }
        binding.addBook.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
        }
    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    var model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                }
                adapterCategory = AdapterCategory(this@AdminDashboard, categoryArrayList)
                binding.categories.adapter = adapterCategory
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
            val email = firebaseUser.email
        }
    }
}