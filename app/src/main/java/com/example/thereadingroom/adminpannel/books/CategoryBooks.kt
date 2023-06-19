package com.example.thereadingroom.adminpannel.books

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.thereadingroom.databinding.ActivityCategoryBooksBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class CategoryBooks : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBooksBinding
    private lateinit var pdfArrayList: ArrayList<ModelPDF>
    private lateinit var pdfAdapterAdmin: PDFAdapterAdmin

    private companion object{
        const val TAG = "TAG_LIST_ADMIN_TAG"
    }

    private var categoryID = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra("category")!!
        categoryID = intent.getStringExtra("categoryID")!!

        binding.categoryTitle.text = category
        loadPDFList()

        binding.searchBarCB.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                try {
                    pdfAdapterAdmin.filter!!.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPDFList() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryID").equalTo(categoryID)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPDF::class.java)

                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryID}")
                        }
                    }
                    pdfAdapterAdmin = PDFAdapterAdmin(this@CategoryBooks, pdfArrayList)
                    binding.categories.adapter = pdfAdapterAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}