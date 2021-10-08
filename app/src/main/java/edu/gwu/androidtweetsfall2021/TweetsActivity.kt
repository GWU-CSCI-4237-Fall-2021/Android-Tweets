package edu.gwu.androidtweetsfall2021

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class TweetsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweets)

        // Retrieve data from the Intent that launched this screen
        val intent: Intent = getIntent()
        val location: String = intent.getStringExtra("LOCATION")!!

        // Kotlin-shorthand for setTitle(...)
        // getString(...) reads from strings.xml and allows you to substitute in any formatting arguments
        val title = getString(R.string.tweets_title, location)
        setTitle(title)

        // val tweets: List<Tweet> = getFakeTweets()
        recyclerView = findViewById(R.id.recyclerView)

        // Sets scrolling direction to vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        val twitterManager: TwitterManager = TwitterManager()
        val twitterApiKey = getString(R.string.twitter_api_key)

        doAsync {
            val tweets: List<Tweet> = try {
                twitterManager.retrieveTweets(twitterApiKey, 37.781157, -122.398720)
            } catch(exception: Exception) {
                Log.e("TweetsActivity", "Retrieving Tweets failed!", exception)
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

    fun getFakeTweets(): List<Tweet> {
        return listOf(
            Tweet(
                handle = "@nickcapurso",
                username = "Nick Capurso",
                content = "We're learning lists!",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "Android Central",
                handle = "@androidcentral",
                content = "NVIDIA Shield TV vs. Shield TV Pro: Which should I buy?",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "DC Android",
                handle = "@DCAndroid",
                content = "FYI - another great integration for the @Firebase platform",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "KotlinConf",
                handle = "@kotlinconf",
                content = "Can't make it to KotlinConf this year? We have a surprise for you. We'll be live streaming the keynotes, closing panel and an entire track over the 2 main conference days. Sign-up to get notified once we go live!",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "Android Summit",
                handle = "@androidsummit",
                content = "What a #Keynote! @SlatteryClaire is the Director of Performance at Speechless, and that's exactly how she left us after her amazing (and interactive!) #keynote at #androidsummit. #DCTech #AndroidDev #Android",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "Fragmented Podcast",
                handle = "@FragmentedCast",
                content = ".... annnnnnnnnd we're back!\n\nThis week @donnfelker talks about how it's Ok to not know everything and how to set yourself up mentally for JIT (Just In Time [learning]). Listen in here: \nhttp://fragmentedpodcast.com/episodes/135/ ",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "Jake Wharton",
                handle = "@JakeWharton",
                content = "Free idea: location-aware physical password list inside a password manager. Mostly for garage door codes and the like. I want to open my password app, switch to the non-URL password section, and see a list of things sorted by physical distance to me.",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "Droidcon Boston",
                handle = "@droidconbos",
                content = "#DroidconBos will be back in Boston next year on April 8-9!",
                iconUrl = "https://...."
            ),
            Tweet(
                username = "AndroidWeekly",
                handle = "@androidweekly",
                content = "Latest Android Weekly Issue 327 is out!\nhttp://androidweekly.net/ #latest-issue  #AndroidDev",
                iconUrl = "https://...."
            ),
            Tweet(
                username = ".droidconSF",
                handle = "@droidconSF",
                content = "Drum roll please.. Announcing droidcon SF 2018! November 19-20 @ Mission Bay Conference Center. Content and programming by @tsmith & @joenrv.",
                iconUrl = "https://...."
            )
        )
    }
}