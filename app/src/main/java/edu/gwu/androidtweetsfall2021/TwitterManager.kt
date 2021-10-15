package edu.gwu.androidtweetsfall2021

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class TwitterManager {
    val okHttpClient: OkHttpClient

    init {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClientBuilder.addInterceptor(loggingInterceptor)

        okHttpClient = okHttpClientBuilder.build()
    }

    fun retrieveOAuthToken(apiKey: String, apiSecret: String): String {
        // URL encoding converts any special characters into the ASCII representations
        // i.e. ' ' --> '%20'
        val encodedKey: String = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret: String = URLEncoder.encode(apiSecret, "UTF-8")

        val concatenatedCredentials: String = "$encodedKey:$encodedSecret"

        val base64Encoding: String = Base64.encodeToString(
            concatenatedCredentials.toByteArray(), Base64.NO_WRAP)

        val bodyString = "grant_type=client_credentials"
        val contentType = "application/x-www-form-urlencoded".toMediaType()
        val postBody = bodyString.toRequestBody(contentType)

        val request: Request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $base64Encoding")
            .post(postBody)
            .build()

        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        return if (response.isSuccessful && !responseBody.isNullOrBlank()) {
            val json: JSONObject = JSONObject(responseBody)
            json.getString("access_token")
        } else {
            ""
        }
    }

    fun retrieveTweets(oAuthToken: String, lat: Double, lon: Double): List<Tweet> {
        val tweets: MutableList<Tweet> = mutableListOf()
        val searchTerm: String = "Android"
        val searchRadius: String = "30mi"

        // Unlike normal API Keys (like Google Maps and News API) Twitter uses something slightly different,
        // so the "apiKey" here isn't really an API Key - we'll see in Lecture 7.
        val request: Request = Request.Builder()
            .url("https://api.twitter.com/1.1/search/tweets.json?q=$searchTerm&geocode=$lat,$lon,$searchRadius")
            .get()
            .addHeader("Authorization", "Bearer $oAuthToken")
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