package com.example.thereadingroom.adminpannel.books

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.thereadingroom.*
import com.example.thereadingroom.databinding.ActivityBookDetailsBinding
import com.example.thereadingroom.databinding.NewCommentDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.util.jar.Manifest

class BookDetails : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding

    private var bookID = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private var isInFavourites = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recommendationArrayList: ArrayList<ModelRecommendation>
    private lateinit var recommendationAdapter: RecommendationAdapter

    private lateinit var progressDialog: ProgressDialog

    private companion object{
        const val TAG = "BOOK_DETAILS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()


        bookID = intent.getStringExtra("bookID")!!

        MyApp.incrementBookViewCount(bookID)
        loadBookDetails()

        showRecommendations()

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.readBook.setOnClickListener {
            val intent = Intent(this, ReadBook::class.java)
            intent.putExtra("bookID", bookID)
            startActivity(intent)
        }
        binding.downloadBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: permission granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: permission not granted")
                requestPermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.addComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "Please login to proceed!", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
    }

    private fun showRecommendations() {
        recommendationArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID).child("Comments")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recommendationArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelRecommendation::class.java)
                        recommendationArrayList.add(model!!)
                    }
                    recommendationAdapter = RecommendationAdapter(this@BookDetails, recommendationArrayList)
                    binding.recommendationRecycle.adapter = recommendationAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment = ""

    private fun addCommentDialog() {
        val newCommentAddBinding = NewCommentDialogBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this, R.style.customDialog)
        builder.setView(newCommentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        newCommentAddBinding.back.setOnClickListener {
            alertDialog.dismiss()
        }
        newCommentAddBinding.submitComment.setOnClickListener {
            comment = newCommentAddBinding.commentPart.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(this, "Enter comment ...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }

    }

    private fun addComment() {
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookID"] = "$bookID"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comments added ...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Comment adding failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "onCreate: permission granted")
            downloadBook()
        } else {
            Log.d(TAG, "onCreate: permission denied")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun downloadBook() {
        progressDialog.setMessage("Downloading book...")
        progressDialog.show()

        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.PDF_MAX_BYTES)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: book downloaded")
                saveToPhone(bytes)
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: downloading failed due to ${e.message}")
                Toast.makeText(this, "Downloading failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToPhone(bytes: ByteArray) {
        Log.d(TAG, "saveToPhone: saving to the device")
        val name = "$bookTitle" + " " + "${System.currentTimeMillis()}.pdf"
        try {
            val dwnFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dwnFolder.mkdirs()

            val filePath = dwnFolder.path + "/" + name
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Successfully saved", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Successfully saved")
            progressDialog.dismiss()
            incrementDownloadCounts()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToPhone: failed to save due to ${e.message}")
            Toast.makeText(this, "Saving failed due to ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun incrementDownloadCounts() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    val newDwnCount: Long = downloadsCount.toLong() + 1
                    val hashMap = HashMap<String, Any>()
                    hashMap["downloadsCount"] = newDwnCount

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookID)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: incrementation successful")
                        }
                        .addOnFailureListener { e->
                            Log.d(TAG, "onDataChange: incrementation failed due to ${e.message}")
                        }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryID = "${snapshot.child("categoryID").value}"
                    val description = "${snapshot.child("description").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val author = "${snapshot.child("author").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"

                    val date = MyApp.formatTimeStamp(timestamp.toLong())

                    MyApp.loadCategory(categoryID, binding.singleBookCategory)
                    MyApp.loadPDFFromUrl("$bookUrl", "$bookTitle", binding.imageCategoryBooks, binding.progressBar, binding.singleBookPages)
                    MyApp.loadPDFSize("$bookUrl", "$bookTitle", binding.singleBookSize)

                    binding.singleBookTitle.text = bookTitle
                    binding.singleBookAuthor.text = author
                    binding.singleBookDesc.text = description
                    binding.singleBookViews.text = viewsCount
                    binding.singleBookDownloads.text = downloadsCount
                    binding.singleBookDate.text = date

                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}