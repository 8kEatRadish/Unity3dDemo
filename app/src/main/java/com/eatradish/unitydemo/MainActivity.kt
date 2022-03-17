package com.eatradish.unitydemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.eatradish.unitydemo.databinding.ActivityMainBinding
import com.unity3d.player.UnityPlayer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.openUnity3d.setOnClickListener {
            startActivity(Intent(this, UnityPlayerActivity::class.java))
        }
    }
}