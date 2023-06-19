package com.example.thereadingroom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.thereadingroom.databinding.ActivityInfoBinding
import com.example.thereadingroom.user.Home
import com.example.thereadingroom.user.Library
import com.google.firebase.auth.FirebaseAuth

class Info : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.homeIcon.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
        binding.libraryIcon.setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }
        binding.settings.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
            finish()
        }

        binding.nul.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://nub.ba")
                )
            )
        }
        binding.ghbl.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://ghb.ba")
                )
            )
        }
        binding.kbl.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://medresamostar.com")
                )
            )
        }
        binding.oldest.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.britishmuseum.org/blog/library-fit-king")
                )
            )
        }
    }
}