package edu.gwu.androidtweetsfall2021

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TweetsAdapter(val tweets: List<Tweet>) : RecyclerView.Adapter<TweetsAdapter.ViewHolder>() {

    // How many rows (total) do you want the adapter to render?
    override fun getItemCount(): Int {
        return tweets.size
    }

    // The RecyclerView needs a "fresh" / new row, so we need to:
    // 1. Read in the XML file for the row type
    // 2. Use the new row to build a ViewHolder to return
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // A LayoutInflater is an object that knows how to read & parse an XML file
        val layoutInflater = LayoutInflater.from(parent.context)

        // Read & parse the XML file to create a new row at runtime
        // The 'inflate' function returns a reference to the root layout (the "top" view in the hierarchy) in our newly created row
        val rootLayout: View = layoutInflater.inflate(R.layout.row_tweet, parent, false)

        // We can now create a ViewHolder from the root view
        return ViewHolder(rootLayout)
    }

    // The RecyclerView is ready to display a new (or recycled) row on the screen, represented a our ViewHolder.
    // We're given the row position / index that needs to be rendered.
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currTweet = tweets[position]
        viewHolder.username.text = currTweet.username
        viewHolder.handle.text = currTweet.handle
        viewHolder.content.text = currTweet.content

        // iconUrl will come later (once we learn how to do image downloads)
    }

    // A ViewHolder represents the Views that comprise a single row in our list (e.g.
    // our row to display a Tweet contains three TextViews and one ImageView).
    //
    // The "rootLayout" passed into the constructor comes from onCreateViewHolder. From the root layout, we can
    // call findViewById to search through the hierarchy to find the Views we care about in our new row.
    class ViewHolder(rootLayout: View) : RecyclerView.ViewHolder(rootLayout) {
        val username: TextView = rootLayout.findViewById(R.id.username)
        val handle: TextView = rootLayout.findViewById(R.id.handle)
        val content: TextView = rootLayout.findViewById(R.id.tweet_content)
        val icon: ImageView = rootLayout.findViewById(R.id.icon)
    }
}