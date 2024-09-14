package com.interview.jenis

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.interview.jenis.adapter.ContactAdapter
import com.interview.jenis.databinding.ActivityMainBinding
import com.interview.jenis.db.ContactDatabase
import com.interview.jenis.viewmodel.ContactViewModel
import com.interview.jenis.viewmodel.ContactViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ContactAdapter
    private val animatedPositions = mutableSetOf<Int>()
    private val contactViewModel: ContactViewModel by viewModels {
        ContactViewModelFactory(ContactDatabase.getDatabase(this))
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            fetchAndDisplayContacts()
        } else {
            Toast.makeText(this, "Permission denied. Cannot display contacts.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (isContactPermissionGranted()) {
            fetchAndDisplayContacts()
        } else {
            requestContactPermission()
        }
        contactViewModel.fetchDeviceContacts(contentResolver)
    }

    private fun requestContactPermission() {
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Toast.makeText(this, "Contacts permission is needed to display contacts.", Toast.LENGTH_SHORT).show()
        }
        requestPermissionLauncher.launch(READ_CONTACTS)
    }

    private fun fetchAndDisplayContacts() {
        contactAdapter = ContactAdapter(listOf())
        binding.recyclerView.apply {
            adapter = contactAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                        for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                            if (!animatedPositions.contains(i)) {
                                val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
                                viewHolder?.itemView?.let { view ->
                                    applyScaleAnimation(view)
                                    animatedPositions.add(i)
                                }
                            }
                        }
                    }
                }
            })
        }

        contactViewModel.fetchAndSaveContacts(contentResolver)

        contactViewModel.getAllContacts().observe(this) { contacts ->
            contactAdapter.updateContacts(contacts)
        }
    }

    private fun applyScaleAnimation(view: View) {
        // Set initial scale smaller
        view.scaleX = 0.8f
        view.scaleY = 0.8f

        // Animate to full size
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(300)
            .start()
    }

    private fun isContactPermissionGranted() = ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
}