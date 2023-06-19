package com.example.thereadingroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thereadingroom.databinding.ActivityMainBinding
import com.example.thereadingroom.login.Login

class Main : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            overridePendingTransition(R.anim.first, R.anim.second)
        }

    }

}