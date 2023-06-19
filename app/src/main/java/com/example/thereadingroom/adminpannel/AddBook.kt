package com.example.thereadingroom.adminpannel

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.thereadingroom.R
import com.example.thereadingroom.databinding.ActivityAddBookBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class AddBook : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private var pdfUri: Uri? = null
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPDFCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.bookCategory.setOnClickListener {
            categoryPickDialog()
        }
        binding.uploadPDFEnglish.setOnClickListener {
            pdfSelectIntent()
        }
        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.submit.setOnClickListener {
            validateData()
        }

    }


    private var title = ""
    private var author = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        title = binding.bookTitle.text.toString().trim()
        author = binding.bookAuthor.text.toString().trim()
        description = binding.bookDescription.text.toString().trim()
        category = binding.bookCategory.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a book title", Toast.LENGTH_SHORT).show()
        } else if (author.isEmpty()) {
            Toast.makeText(this, "Please enter a book author", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a book description", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Please select a book category", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Please select a book pdf file", Toast.LENGTH_SHORT).show()
        } else {
            uploadData()
        }
    }

    private fun uploadData() {
        Log.d(TAG, "uploadData: uploading data")
        progressDialog.setMessage("Uploading data...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Books/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadData: upload successful")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPDFUrl = "${uriTask.result}"

                uploadPDFInfoToDatabase(uploadPDFUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadData: upload failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPDFInfoToDatabase(uploadPDFUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPDFInfoToDatabase: uploading to database")
        progressDialog.setMessage("Uploading to database...")

        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["author"] = "$author"
        hashMap["description"] = "$description"
        hashMap["categoryID"] = "$selectedCategoryID"
        hashMap["url"] = "$uploadPDFUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadData: uploaded to database")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                pdfUri = null
                startActivity(Intent(this, AdminDashboard::class.java))
                overridePendingTransition(R.anim.first, R.anim.second)
                finish()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "uploadData: upload failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPDFCategories() {
        Log.d(TAG, "loadPDFCategories: Loading PDF categories")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private var selectedCategoryID = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please select a book category")
            .setItems(categoriesArray) { dialog, which ->
                selectedCategoryID = categoryArrayList[which].id
                selectedCategoryTitle = categoryArrayList[which].category

                binding.bookCategory.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected categoryID: $selectedCategoryID")
                Log.d(TAG, "categoryPickDialog: Selected categoryTitle: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfSelectIntent() {
        Log.d(TAG, "pdfSelectIntent: starting pdf select intent")
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)

    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Selected")
                Toast.makeText(this, "PDF Selected", Toast.LENGTH_SHORT).show()
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Selection cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}