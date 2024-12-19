package com.bangkit.mystory.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.mystory.R
import com.bangkit.mystory.data.remote.response.ListStoryItem
import com.bangkit.mystory.databinding.ItemRowStoriesBinding
import com.bumptech.glide.Glide

class StoryListAdapter(
    private val onItemClicked: (ListStoryItem) -> Unit
) : RecyclerView.Adapter<StoryListAdapter.StoryViewHolder>() {

    private val stories = ArrayList<ListStoryItem>()

    fun setStories(storyList: List<ListStoryItem>) {
        Log.d("StoryListAdapter", "New Stories: ${storyList.size}")
        stories.clear()
        stories.addAll(storyList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemRowStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    inner class StoryViewHolder(private val binding: ItemRowStoriesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.tvItemName.text = story.name
            binding.tvContent.text = story.description
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.ivItemPhoto)

            itemView.setOnClickListener {
                onItemClicked(story)
            }
        }
    }
}