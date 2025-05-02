// MainActivity.kt
package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

        setupBottomNavigation()
        Log.d(TAG, "Abrindo HomeFragment")
        openFragment(HomeFragment())
    }

    fun updateMiniPlayer(track: Track) {
        miniPlayerController.bind(track)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Navegando para HomeFragment")
                    openFragment(HomeFragment())
                }
                R.id.nav_search -> {
                    Log.d(TAG, "Navegando para SearchFragment")
                    openFragment(SearchFragment())
                }
                R.id.nav_library -> {
                    Log.d(TAG, "Navegando para LibraryFragment")
                    openFragment(LibraryFragment())
                }
            }
            true
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
