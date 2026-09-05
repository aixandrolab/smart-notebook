// Copyright (c) 2026, Alexander Suvorov. All rights reserved.
package com.example.smart_notebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LinkAdapter(
    private val links: List<Link>,
    private val onLinkClick: (Link) -> Unit,
    private val onLinkLongClick: (Link) -> Unit
) : RecyclerView.Adapter<LinkAdapter.LinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_link, parent, false)
        return LinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        val link = links[position]
        holder.bind(link)
    }

    override fun getItemCount(): Int = links.size

    inner class LinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.linkTitleText)
        private val urlText: TextView = itemView.findViewById(R.id.linkUrlText)
        private val icon: ImageView = itemView.findViewById(R.id.linkIcon)
        private val openIcon: ImageView = itemView.findViewById(R.id.linkOpenIcon)

        fun bind(link: Link) {
            titleText.text = link.title
            urlText.text = link.url

            val iconRes = when {
                link.url.contains("youtube.com") || link.url.contains("youtu.be") -> R.drawable.ic_youtube
                link.url.contains("github.com") -> R.drawable.ic_github
                link.url.contains("wikipedia.org") -> R.drawable.ic_wikipedia
                link.url.contains("google.com") -> R.drawable.ic_google
                link.url.contains("facebook.com") -> R.drawable.ic_facebook
                link.url.contains("twitter.com") || link.url.contains("x.com") -> R.drawable.ic_twitter
                link.url.contains("instagram.com") -> R.drawable.ic_instagram
                link.url.contains("linkedin.com") -> R.drawable.ic_linkedin
                link.url.contains("reddit.com") -> R.drawable.ic_reddit
                link.url.contains("amazon.com") || link.url.contains("amazon.") -> R.drawable.ic_amazon
                link.url.contains("stackoverflow.com") -> R.drawable.ic_stackoverflow
                link.url.contains("medium.com") -> R.drawable.ic_medium
                link.url.contains("telegram.org") -> R.drawable.ic_telegram
                link.url.contains("whatsapp.com") -> R.drawable.ic_whatsapp
                else -> R.drawable.ic_link
            }
            icon.setImageResource(iconRes)

            openIcon.setImageResource(R.drawable.ic_open_in_new)

            itemView.setOnClickListener { onLinkClick(link) }
            itemView.setOnLongClickListener {
                onLinkLongClick(link)
                true
            }
        }
    }
}