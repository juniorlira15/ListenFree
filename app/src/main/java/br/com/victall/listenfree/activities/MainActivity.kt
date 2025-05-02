// MainActivity.kt
package br.com.victall.listenfree.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
        Log.d(TAG, "onCreate iniciado")
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Layout inflado")

        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase inicializado")

        instance = this

        miniPlayerController = MiniPlayerController(binding.miniPlayer) {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        // Solicita permissão de notificação se necessário
        requestNotificationPermission()

        setupBottomNavigation()
        Log.d(TAG, "Abrindo HomeFragment")
        openFragment(HomeFragment())
    }

    fun updateMiniPlayer(track: Track) {
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
        Log.d(TAG, "Abrindo fragment: ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        Log.d(TAG, "Fragment transaction commitado")
    }
}
