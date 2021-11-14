package edu.gwu.androidtweetsfall2021

import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var addTweets: FloatingActionButton
    private lateinit var tweetContent: EditText
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val currentTweets: MutableList<Tweet> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        addTweets = findViewById(R.id.add_tweet)
        tweetContent = findViewById(R.id.tweet_content)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve data from the Intent that launched this screen
        val intent: Intent = getIntent()
        val address: Address = intent.getParcelableExtra("address")!!

        if (savedInstanceState != null) {
            // Activity has just been rotated and our state has been saved in the bundle
            currentTweets.addAll(savedInstanceState.getSerializable("tweets") as List<Tweet>)
            val adapter = TweetsAdapter(currentTweets)

            addTweets.hide()
            tweetContent.visibility = View.GONE

            recyclerView.adapter = adapter
        } else {
            // First time Activity launch, retrieve data from Twitter / Firebase
            getTweetsFromTwitter(address)
            //getTweetsFromFirebase(address)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val serializableList = ArrayList(currentTweets)
        outState.putSerializable("tweets", serializableList)
    }

    private fun getTweetsFromFirebase(address: Address) {
        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        val state = address.adminArea ?: "Unknown"
        val title = getString(R.string.tweets_title, state)
        setTitle(title)

        val reference = firebaseDatabase.getReference("tweets/$state")
        addTweets.setOnClickListener {
            firebaseAnalytics.logEvent("add_tweets_clicked", null)
            val content = tweetContent.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (content.isNotEmpty()) {
                val username = currentUser!!.email!!
                val handle = username
                val tweet = Tweet(username, handle, content, "https://i.imgur.com/DvpvklR.png") // Fake image URL for now

                reference.push().setValue(tweet)
            }

        }

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                firebaseAnalytics.logEvent("firebasedb_cancelled", null)
                Toast.makeText(
                    this@TweetsActivity,
                    "Failed to retrieve Tweets! Error: ${databaseError.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                firebaseAnalytics.logEvent("firebasedb_data_change", null)
                val tweets = mutableListOf<Tweet>()
                dataSnapshot.children.forEach { data ->
                    try {
                        val tweet = data.getValue(Tweet::class.java)
                        if (tweet != null) {
                            tweets.add(tweet)
                        }
                    } catch (exception: Exception) {
                        Log.e("TweetsActivity", "Failed to read Tweet", exception)
                        Firebase.crashlytics.recordException(exception)
                    }
                }
                recyclerView.adapter = TweetsAdapter(tweets)
            }
        })


    }

    private fun getTweetsFromTwitter(address: Address) {
        addTweets.hide()
        tweetContent.visibility = View.GONE

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        val title = getString(R.string.tweets_title, address.getAddressLine(0))
        setTitle(title)

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

            currentTweets.clear()
            currentTweets.addAll(tweets)

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