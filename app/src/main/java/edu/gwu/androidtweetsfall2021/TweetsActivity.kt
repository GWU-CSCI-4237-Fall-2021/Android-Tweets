package edu.gwu.androidtweetsfall2021

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TweetsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        val intent: Intent = getIntent()
        val location: String = intent.getStringExtra("LOCATION")!!

        val title = getString(R.string.tweets_title, location)
        setTitle(title)
    }
}