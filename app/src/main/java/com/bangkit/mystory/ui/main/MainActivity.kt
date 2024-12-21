package com.bangkit.mystory.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.mystory.R
import com.bangkit.mystory.databinding.ActivityMainBinding
import com.bangkit.mystory.ui.ViewModelFactory
import com.bangkit.mystory.ui.adapter.LoadingStateAdapter
import com.bangkit.mystory.ui.adapter.StoryListAdapter
import com.bangkit.mystory.ui.addstory.AddStoryActivity
import com.bangkit.mystory.ui.detail.DetailActivity
import com.bangkit.mystory.ui.maps.MapsActivity
import com.bangkit.mystory.ui.onboarding.WelcomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var storyAdapter: StoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        observeLogin()
        setupFabAction()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryListAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY, story)
            startActivity(intent)
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter.withLoadStateHeaderAndFooter(
                header = LoadingStateAdapter { storyAdapter.retry() },
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }

        storyAdapter.addLoadStateListener { loadState ->
            binding.progressBar.isVisible = loadState.source.refresh is androidx.paging.LoadState.Loading
            binding.rvStories.isVisible = loadState.source.refresh is androidx.paging.LoadState.NotLoading
            if (loadState.source.refresh is androidx.paging.LoadState.Error) {
                val errorMessage = (loadState.source.refresh as androidx.paging.LoadState.Error).error.localizedMessage
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeLogin() {
        mainViewModel.getLogin().observe(this) { user ->
            if (user.isLogin) {
                observeStories(user.token)
            } else {
                navigateToWelcome()
            }
        }
    }

    private fun observeStories(token: String) {
        mainViewModel.getStories(token).observe(this, Observer { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
        })
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun setupFabAction() {
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                navigateToMap()
                true
            }
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToMap() {
        mainViewModel.getLogin().observe(this) { user ->
            if (user.isLogin) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra(MapsActivity.EXTRA_TOKEN, user.token)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.error_missing_token, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogout() {
        mainViewModel.deleteLogin()
        Toast.makeText(this, R.string.logout_succes, Toast.LENGTH_SHORT).show()
        navigateToWelcome()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}