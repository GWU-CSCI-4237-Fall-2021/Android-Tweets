package edu.gwu.androidtweetsfall2021

import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var addTweets: FloatingActionButton
    private lateinit var tweetContent: EditText
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        addTweets = findViewById(R.id.add_tweet)
        tweetContent = findViewById(R.id.tweet_content)

        // Retrieve data from the Intent that launched this screen
        val intent: Intent = getIntent()
        val address: Address = intent.getParcelableExtra("address")!!

        getTweetsFromTwitter(address)
    }

    private fun getTweetsFromTwitter(address: Address) {

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        val title = getString(R.string.tweets_title, address.getAddressLine(0))
        setTitle(title)

        // val tweets: List<Tweet> = getFakeTweets()
        recyclerView = findViewById(R.id.recyclerView)

        // Sets scrolling direction to vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        val twitterManager: TwitterManager = TwitterManager()
        val twitterApiKey = getString(R.string.twitter_api_key)
        val twitterApiSecret = getString(R.string.twitter_api_secret)

        doAsync {
            val tweets: List<Tweet> = try {
                val oAuthToken = twitterManager.retrieveOAuthToken(twitterApiKey, twitterApiSecret)
                twitterManager.retrieveTweets(oAuthToken, address.latitude, address.longitude).also {
                    firebaseAnalytics.logEvent("twitter_success", null)
                }
            } catch(exception: Exception) {
                Log.e("TweetsActivity", "Retrieving Tweets failed!", exception)
                Firebase.crashlytics.recordException(exception)
                firebaseAnalytics.logEvent("twitter_failed", null)
                listOf<Tweet>()
            }

            runOnUiThread {
                if (tweets.isNotEmpty()) {
                    val adapter: TweetsAdapter = TweetsAdapter(tweets)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(
                        this@TweetsActivity,
                        "Failed to retrieve Tweets!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }
}