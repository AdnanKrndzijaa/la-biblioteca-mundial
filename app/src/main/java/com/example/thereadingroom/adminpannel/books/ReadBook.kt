package com.example.thereadingroom.adminpannel.books

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.thereadingroom.Constants
import com.example.thereadingroom.R
import com.example.thereadingroom.databinding.ActivityReadBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ReadBook : AppCompatActivity() {
    private lateinit var binding: ActivityReadBookBinding

    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    var mediaPlayer: MediaPlayer? = null

    var bookID = ""
    private companion object{
        const val TAG = "PDF_READ_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookID = intent.getStringExtra("bookID")!!
        loadBookDetails()
        
        binding.back.setOnClickListener { 
            onBackPressed()
        }

        binding.playMusic.setOnClickListener {
            playMusic()
        }

        binding.stopMusic.setOnClickListener {
            stopMusic()
        }

    }

    private fun stopMusic() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        } else {
            Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playMusic() {
        val audioUrl = "https://www.bensound.com/bensound-music/bensound-ukulele.mp3"
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

        try {
            mediaPlayer!!.setDataSource(audioUrl)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Toast.makeText(this, "Audio started playing", Toast.LENGTH_SHORT).show()
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: get pdf from database")
        
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookID)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val url = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: pdf url is $url")

                    loadBookDatabase("$url")
                }
                override fun onCancelled(error: DatabaseError) {
                    
                }
            })
    }

    private fun loadBookDatabase(pdfUrl: String) {
        Log.d(TAG, "loadBookDatabase: get pdf from firebase")

        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        ref.getBytes(Constants.PDF_MAX_BYTES)
            .addOnSuccessListener { b->
                Log.d(TAG, "loadBookDatabase: Successful")

                binding.pdfView.fromBytes(b)
                    .swipeHorizontal(false)
                    .onPageChange{ p, count ->
                        val currentPage = p + 1
                        binding.numberOfPages.text = "$currentPage/$count"
                        Log.d(TAG, "loadBookDatabase: $currentPage/$count")
                    }
                    .onError{ t->
                        Log.d(TAG, "loadBookDatabase: error: ${t.message}")
                    }
                    .onPageError { p, t ->
                        Log.d(TAG, "loadBookDatabase: error: ${t.message}")
                    }
                    .load()

                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                Log.d(TAG, "loadBookDatabase: failed du to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}