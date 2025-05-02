package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.victall.listenfree.adapters.PodcastAdapter
import br.com.victall.listenfree.databinding.ActivityPodcastPreferredBinding
import br.com.victall.listenfree.models.Podcast

class PodcastPreferredActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPodcastPreferredBinding
    private lateinit var podcastAdapter: PodcastAdapter
    
    private var email: String? = null
    private var password: String? = null
    private var gender: String? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPodcastPreferredBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")
        gender = intent.getStringExtra("gender")
        name = intent.getStringExtra("name")

        setupViews()
        setupRecyclerView()
        loadPodcasts()
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
                    filterPodcasts(s?.toString() ?: "")
                }
            })

            btnDone.setOnClickListener {
                // Aqui você pode implementar a criação da conta com todos os dados coletados
                navigateToMain()
            }
        }
    }

    private fun setupRecyclerView() {
        podcastAdapter = PodcastAdapter(
            onPodcastClick = { podcast ->
                // Opcional: você pode implementar alguma lógica quando um podcast é selecionado
            },
            onFavoriteClick = { podcast ->
                // Aqui você pode implementar a lógica de favorito
                // Por exemplo, salvar no banco de dados local ou enviar para a API
            }
        )

        binding.rvPodcasts.apply {
            layoutManager = LinearLayoutManager(this@PodcastPreferredActivity)
            adapter = podcastAdapter
        }
    }

    private fun loadPodcasts() {
        // TODO: Carregar lista de podcasts da API
        // Por enquanto, vamos usar dados mockados
        val mockPodcasts = listOf(
            Podcast("1", "Joe Rogan Experience", "O maior podcast do mundo", "https://example.com/jre.jpg", "Joe Rogan"),
            Podcast("2", "Serial", "True crime podcast", "https://example.com/serial.jpg", "Sarah Koenig"),
            Podcast("3", "TED Radio Hour", "Ideas worth spreading", "https://example.com/ted.jpg", "NPR"),
            Podcast("4", "Hardcore History", "História em detalhes", "https://example.com/hardcore.jpg", "Dan Carlin"),
            Podcast("5", "This American Life", "Histórias reais", "https://example.com/tal.jpg", "Ira Glass")
        )
        podcastAdapter.submitList(mockPodcasts)
    }

    private fun filterPodcasts(query: String) {
        // TODO: Implementar filtro de podcasts baseado na query
    }

    private fun navigateToMain() {
        // TODO: Implementar navegação para a tela principal
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}