package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.victall.listenfree.adapters.ArtistAdapter
import br.com.victall.listenfree.databinding.ActivityArtistPreferredBinding
import br.com.victall.listenfree.models.Artist

class ArtistPreferredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtistPreferredBinding
    private lateinit var artistAdapter: ArtistAdapter
    
    private var email: String? = null
    private var password: String? = null
    private var gender: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistPreferredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")
        gender = intent.getStringExtra("gender")
        name = intent.getStringExtra("name")

        setupViews()
        setupRecyclerView()
        loadArtists()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    filterArtists(s?.toString() ?: "")
                }
            })

            btnNext.setOnClickListener {
                if (artistAdapter.getSelectedCount() >= 3) {
                    navigateToPodcastPreferred()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        artistAdapter = ArtistAdapter { artist, isSelected ->
            updateNextButtonState()
        }

        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(this@ArtistPreferredActivity)
            adapter = artistAdapter
        }
    }

    private fun updateNextButtonState() {
        binding.btnNext.isEnabled = artistAdapter.getSelectedCount() >= 3
    }

    private fun loadArtists() {
        // TODO: Carregar lista de artistas da API
        // Por enquanto, vamos usar dados mockados
        val mockArtists = listOf(
            Artist("1", "The Beatles", "https://example.com/beatles.jpg"),
            Artist("2", "Queen", "https://example.com/queen.jpg"),
            Artist("3", "Pink Floyd", "https://example.com/pinkfloyd.jpg"),
            Artist("4", "Led Zeppelin", "https://example.com/ledzeppelin.jpg"),
            Artist("5", "Rolling Stones", "https://example.com/rollingstones.jpg")
        )
        artistAdapter.submitList(mockArtists)
    }

    private fun filterArtists(query: String) {
        // TODO: Implementar filtro de artistas baseado na query
    }

    private fun navigateToPodcastPreferred() {
        val intent = Intent(this, PodcastPreferredActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
            putExtra("gender", gender)
            putExtra("name", name)
            // TODO: Adicionar lista de artistas selecionados
        }
        startActivity(intent)
    }
} 