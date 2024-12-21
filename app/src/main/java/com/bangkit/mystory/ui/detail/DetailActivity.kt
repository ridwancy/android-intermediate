package com.bangkit.mystory.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.mystory.R
import com.bangkit.mystory.data.remote.response.ListStoryItem
import com.bangkit.mystory.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_title)

        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY)
        if (story != null) {
            bindData(story)
        }
    }

    private fun bindData(story: ListStoryItem) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        binding.tvCreatedAt.text = story.createdAt
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)

        val locationText = if (story.lat != null && story.lon != null) {
            getString(R.string.location_format, story.lat, story.lon)
        } else {
            getString(R.string.location_not_available)
        }
        binding.tvLocation.text = locationText
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}