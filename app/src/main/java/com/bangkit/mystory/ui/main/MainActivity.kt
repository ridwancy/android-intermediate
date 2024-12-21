package com.bangkit.mystory.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.mystory.R
import com.bangkit.mystory.databinding.ActivityMainBinding
import com.bangkit.mystory.ui.ViewModelFactory
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

    private val storyAdapter = StoryListAdapter { story ->
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupViewModel(savedInstanceState)
        setupRefresh()
        setupFabAction()
    }

    private fun setupRecyclerView() {
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }
    }

    private fun setupViewModel(savedInstanceState: Bundle?) {
        mainViewModel.getLogin().observe(this) { user ->
            Log.d("MainActivity", "user: $user")
            if (user.isLogin) {
                binding.root.visibility = View.VISIBLE


            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private fun fetchStories(token: String) {
        binding.progressBar.visibility = View.VISIBLE
        mainViewModel.getStories(token).observe(this) { result ->
            binding.progressBar.visibility = View.GONE
            result.onSuccess { stories ->
                storyAdapter.setStories(stories)
            }.onFailure {
                Toast.makeText(this, R.string.error_fetching_stories, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRefresh() {
        binding.refresh.setOnRefreshListener {
            mainViewModel.getLogin().observe(this) { user ->
                if (user.isLogin) {
                    fetchStories(user.token)
                }
            }
            binding.refresh.isRefreshing = false
        }
    }

    private val addStoryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mainViewModel.getLogin().observe(this) { user ->
                    if (user.isLogin) {
                        fetchStories(user.token)
                    }
                }
            }
        }

    private fun setupFabAction() {
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryLauncher.launch(intent)
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

    override fun onResume() {
        super.onResume()
        mainViewModel.getLogin().observe(this) { user ->
            if (user.isLogin) {
                fetchStories(user.token)
            }
        }
    }


    private fun performLogout() {
        mainViewModel.deleteLogin()
        Toast.makeText(this, R.string.logout_succes, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}