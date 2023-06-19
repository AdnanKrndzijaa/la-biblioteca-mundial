package com.example.thereadingroom

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.time.Year
import java.util.*
import kotlin.collections.HashMap

class MyApp : Application(){

    override fun onCreate() {
        super.onCreate()
    }

    companion object{
        fun formatTimeStamp(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun formatTimeStampShort(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return DateFormat.format("MMMM yyyy", cal).toString()
        }

        fun loadPDFSize(pdfUrl: String, pdfTitle: String, size: TextView) {
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->
                    Log.d(TAG, "loadPDFSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPDFSize: size in bytes is $bytes")

                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb > 1) {
                        size.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        size.text = "${String.format("%.2f", mb)} KB"
                    } else {
                        size.text = "${String.format("%.2f", mb)} bytes"
                    }
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "loadPDFSize: failed to get metadata due to ${e.message}")
                }
        }

        fun loadPDFFromUrl(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pages: TextView?
        ){
            val TAG = "PDF_THUMBNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.PDF_MAX_BYTES)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPDFSize: size in bytes is $bytes")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError{ t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPDFFromUrl: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPDFFromUrl: ${t.message}")
                        }
                        .onLoad { numberPages ->
                            Log.d(TAG, "loadPDFFromUrl: pages: $numberPages")
                            progressBar.visibility = View.INVISIBLE
                            if (pages != null) {
                                pages.text = "$numberPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "loadPDFSize: failed to get metadata due to ${e.message}")

                }
        }
        fun loadCategory(categoryID: String, categoryy: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryID)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        categoryy.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun deleteBook(context: Context, bookID: String, bookUrl: String, bookTitle: String) {
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Book deleting...")
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting book...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: deleting from storage...")
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: book deleting successful")
                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookID)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Delete successful", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: deleted")
                        }
                        .addOnFailureListener { e->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: book deleting failed due to ${e.message}")
                            Toast.makeText(context, "Delete failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: book deleting failed due to ${e.message}")
                    Toast.makeText(context, "Delete failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookID: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookID)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var views = "${snapshot.child("viewsCount").value}"

                        if (views == "" || views == "null") {
                            views = "0"
                        }

                        val newViews = views.toLong() + 1
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViews

                        val ref = FirebaseDatabase.getInstance().getReference("Books")
                        ref.child(bookID)
                            .updateChildren(hashMap)

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun removeFromFavorite(context: Context, bookID: String) {
            val firebaseAuth = FirebaseAuth.getInstance()

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorite").child(bookID)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Book removed from favourites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Toast.makeText(context, "Failed to remove book from favourites due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}