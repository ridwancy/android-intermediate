package com.bangkit.mystory.ui.addstory

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.mystory.R
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.databinding.ActivityAddStoryBinding
import com.bangkit.mystory.ui.ViewModelFactory
import com.bangkit.mystory.ui.main.MainActivity
import com.bangkit.mystory.ui.main.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedImageFile: File? = null
    private lateinit var user: UserEntity
    private var lat: Double? = null
    private var lon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkAndRequestPermissions()

        addStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(this)
        )[AddStoryViewModel::class.java]

        val mainViewModel: MainViewModel by viewModels {
            ViewModelFactory.getInstance(this)
        }
        mainViewModel.getLogin().observe(this) { loggedInUser ->
            if (loggedInUser != null) {
                user = loggedInUser
            } else {
                finish()
            }
        }

        setupActions()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupActions() {
        binding.cameraButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                launcherCamera.launch(intent)
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

        binding.galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcherGallery.launch(intent)
        }

        binding.cbLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
            } else {
                lat = null
                lon = null
            }
        }

        binding.btnUpload.setOnClickListener {
            uploadStory()
        }
    }

    private fun getMyLastLocation() {
        if (!this::fusedLocationClient.isInitialized) {
            Toast.makeText(this, R.string.error_location_client_not_initialized, Toast.LENGTH_SHORT).show()
            return
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    Log.d("AddStoryActivity", "Location: $lat, $lon")
                } else {
                    Log.d("AddStoryActivity", "Location is null")
                    Toast.makeText(this, R.string.location_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun uploadStory() {
        if (selectedImageFile == null || binding.etCaption.text.isNullOrEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageFile!!.length() > 1_000_000) { // 1 MB
            selectedImageFile = compressImageFile(selectedImageFile!!) // Kompres gambar
        }

        val file = selectedImageFile!!
        val description = RequestBody.create("text/plain".toMediaType(), binding.etCaption.text.toString())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile,
        )

        showLoading(true)
        addStoryViewModel.uploadStory(user.token, multipartBody, description, lat, lon).observe(this) { result ->
            showLoading(false)
            result.onSuccess {
                Toast.makeText(this, R.string.success_upload, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure {
                Toast.makeText(this, R.string.error_upload, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressImageFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val compressedFile = File(cacheDir, "compressed_${file.name}")
        compressedFile.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
        return compressedFile
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private val launcherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as Bitmap
            val tempFile = File(cacheDir, "photo.jpg")
            tempFile.outputStream().use {
                photo.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            selectedImageFile = tempFile
            binding.ivStoryPhoto.setImageBitmap(photo)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        val requiredPermissions = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (requiredPermissions.isNotEmpty()) {
            requestPermissions(requiredPermissions.toTypedArray(), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                getMyLastLocation()
            }
        }
    }

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri = result.data?.data as Uri
            val file = uriToFile(selectedImageUri)
            selectedImageFile = file
            binding.ivStoryPhoto.setImageURI(selectedImageUri)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val tempFile = File.createTempFile("temp", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}