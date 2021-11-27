package edu.gwu.androidtweetsfall2021

import org.json.JSONArray
import org.json.JSONObject

class SearchTweetsResponseParser {
    fun parseJson(json: JSONObject): List<Tweet> {
        val tweets = mutableListOf<Tweet>()
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

        return tweets
    }
}