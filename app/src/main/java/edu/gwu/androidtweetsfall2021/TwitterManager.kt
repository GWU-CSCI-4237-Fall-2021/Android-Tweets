package edu.gwu.androidtweetsfall2021

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject

class TwitterManager {
    val okHttpClient: OkHttpClient

    init {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClientBuilder.addInterceptor(loggingInterceptor)

        okHttpClient = okHttpClientBuilder.build()
    }

    fun retrieveTweets(apiKey: String, lat: Double, lon: Double): List<Tweet> {
        val tweets: MutableList<Tweet> = mutableListOf()
        val searchTerm: String = "Android"
        val searchRadius: String = "30mi"

        // Unlike normal API Keys (like Google Maps and News API) Twitter uses something slightly different,
        // so the "apiKey" here isn't really an API Key - we'll see in Lecture 7.
        val request: Request = Request.Builder()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$searchTerm&geocode=$lat,$lon,$searchRadius")
            .get()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        if (response.isSuccessful && !responseBody.isNullOrBlank()) {
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("statuses")

            for (i in 0 until statuses.length()) {
                val curr: JSONObject = statuses.getJSONObject(i)
                val text: String = curr.getString("text")

                val user: JSONObject = curr.getJSONObject("user")
                val name: String = user.getString("name")
                val handle: String = user.getString("screen_name")
                val profilePictureUrl: String = user.getString("profile_image_url_https")

                val tweet: Tweet = Tweet(
                    username = name,
                    handle = handle,
                    content = text,
                    iconUrl = profilePictureUrl
                )

                tweets.add(tweet)
            }
        }

        return tweets
    }
}