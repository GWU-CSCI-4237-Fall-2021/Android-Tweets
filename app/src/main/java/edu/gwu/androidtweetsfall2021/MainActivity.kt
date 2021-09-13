package edu.gwu.androidtweetsfall2021

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.out.println("onCreate fired")
        Log.d("MainActivity", "onCreate fired")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume fired")
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause fired")
        super.onPause()
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop fired")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy fired")
        super.onDestroy()
    }
}