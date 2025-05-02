// MainActivity.kt
package br.com.victall.listenfree.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.com.victall.listenfree.R
import br.com.victall.listenfree.databinding.ActivityMainBinding
import br.com.victall.listenfree.fragments.HomeFragment
import br.com.victall.listenfree.fragments.SearchFragment
import br.com.victall.listenfree.fragments.LibraryFragment
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager
import br.com.victall.listenfree.ui.MiniPlayerController
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerController: MiniPlayerController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permissão de notificação concedida")
        } else {
            Log.d(TAG, "Permissão de notificação negada")
        }
    }

    companion object {
        lateinit var instance: MainActivity
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        instance = this
        miniPlayerController = MiniPlayerController(binding.miniPlayer) {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        requestNotificationPermission()
        setupBottomNavigation()
        openFragment(HomeFragment())
    }

    override fun onResume() {
        super.onResume()
        PlayerManager.getCurrentTrack()?.let { track ->
            if (PlayerManager.isPlaying()) {
                updateMiniPlayer(track)
            }
        }
    }

    fun updateMiniPlayer(track: Track) {
        binding.miniPlayer.root.visibility = View.VISIBLE
        miniPlayerController.bind(track)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permissão já concedida
                    Log.d(TAG, "Permissão de notificação já concedida")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explica ao usuário por que a permissão é necessária
                    showNotificationPermissionRationale()
                }
                else -> {
                    // Solicita a permissão
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        // Mostra um diálogo explicando por que precisamos da permissão
        android.app.AlertDialog.Builder(this)
            .setTitle("Permissão Necessária")
            .setMessage("Precisamos da permissão de notificação para mostrar os controles de música quando o app estiver em segundo plano.")
            .setPositiveButton("Permitir") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Não Permitir") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Navegando para HomeFragment")
                    openFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    Log.d(TAG, "Navegando para SearchFragment")
                    openFragment(SearchFragment())
                    true
                }
                R.id.nav_library -> {
                    Log.d(TAG, "Navegando para LibraryFragment")
                    openFragment(LibraryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        PlayerManager.getCurrentTrack()?.let { track ->
            if (PlayerManager.isPlaying()) {
                updateMiniPlayer(track)
            }
        }
    }
}
