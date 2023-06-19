package com.example.thereadingroom.adminpannel.books

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.thereadingroom.R
import com.example.thereadingroom.adminpannel.AdminDashboard
import com.example.thereadingroom.databinding.ActivityEditBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditBook : AppCompatActivity() {
    private lateinit var binding: ActivityEditBookBinding
    private var bookID = ""
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryTitleArrayList: ArrayList<String>
    private companion object{
        private const val TAG = "EDIT_TAG"
    }

    private lateinit var categoryIDArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookID = intent.getStringExtra("bookID")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBook()

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.bookCategory.setOnClickListener {
            categoryDialog()
        }
        binding.update.setOnClickListener {
            validateData()
            startActivity(Intent(this, AdminDashboard::class.java))
            finish()
        }
    }

    private fun loadBook() {
        Log.d(TAG, "loadBook: loading book...")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedBookCategoryID = snapshot.child("categoryID").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val author = snapshot.child("author").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.bookTitle.setText(title)
                    binding.bookAuthor.setText(author)
                    binding.bookDescription.setText(description)

                    Log.d(TAG, "onDataChange: loading book category")
                    val refBookCat = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCat.child(selectedBookCategoryID)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value
                                binding.bookCategory.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var author = ""
    private var description = ""

    private fun validateData() {
        title = binding.bookTitle.text.toString().trim()
        author = binding.bookAuthor.text.toString().trim()
        description = binding.bookDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a book title", Toast.LENGTH_SHORT).show()
        } else if (author.isEmpty()) {
            Toast.makeText(this, "Please enter a book author", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a book description", Toast.LENGTH_SHORT).show()
        } else if (selectedBookCategoryID.isEmpty()) {
            Toast.makeText(this, "Please select a book category", Toast.LENGTH_SHORT).show()
        } else {
            updateBook()
        }
    }

    private fun updateBook() {
        Log.d(TAG, "updateBook: starting updating")

        progressDialog.setMessage("Updating book...")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["author"] = "$author"
        hashMap["description"] = "$description"
        hashMap["categoryID"] = "$selectedBookCategoryID"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updateBook: updating successful")
                Toast.makeText(this, "Updating book successful", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "updateBook: updating failed")
                progressDialog.dismiss()
                Toast.makeText(this, "Updating failed du to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedBookCategoryID = ""
    private var selectedBookCategoryTitle = ""

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose category")
            .setItems(categoriesArray) { dialog, position ->
                selectedBookCategoryID = categoryIDArrayList[position]
                selectedBookCategoryTitle = categoryTitleArrayList[position]

                binding.bookCategory.text = selectedBookCategoryTitle
            }
            .show()
    }


    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories")

        categoryTitleArrayList = ArrayList()
        categoryIDArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIDArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIDArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: categoryID id $id")
                    Log.d(TAG, "onDataChange: category is $category")

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}