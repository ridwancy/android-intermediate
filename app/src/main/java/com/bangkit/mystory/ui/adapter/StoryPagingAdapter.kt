package com.bangkit.mystory.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.mystory.R
import com.bangkit.mystory.data.remote.response.ListStoryItem
import com.bangkit.mystory.databinding.ItemRowStoriesBinding
import com.bumptech.glide.Glide

class StoryPagingAdapter(
    private val onItemClicked: (ListStoryItem) -> Unit
) : PagingDataAdapter<ListStoryItem, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemRowStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id // Perbandingan berdasarkan ID
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem // Perbandingan berdasarkan isi
            }
        }
    }


    inner class StoryViewHolder(private val binding: ItemRowStoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
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