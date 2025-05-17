package br.com.victall.listenfree.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import br.com.victall.listenfree.R
import br.com.victall.listenfree.core.services.FirebaseService
import br.com.victall.listenfree.github.GitHubManager
import br.com.victall.listenfree.models.Album
import kotlinx.coroutines.launch

class AlbumCreateActivity : AppCompatActivity() {

    private lateinit var firebaseService: FirebaseService
    private lateinit var userId: String
    private var selectedCoverUri: Uri? = null
    private val userGit = "juniorlira16"
    private val repoGit = "listenfree-assets"
    private val token: String = "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"

    // depois: FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val albumId = intent.getStringExtra("albumId").orEmpty()
        val albumNameFromIntent = intent.getStringExtra("albumName").orEmpty()
        setContentView(R.layout.activity_album_create)

        firebaseService = FirebaseService()
        userId = "uid123"

        val etTitulo = findViewById<EditText>(R.id.etTitulo)
        val etArtista = findViewById<EditText>(R.id.etArtista)
        val etGenero = findViewById<EditText>(R.id.etGenero)
        val etAno = findViewById<EditText>(R.id.etAno)
        val etCapa = findViewById<EditText>(R.id.etCapa)
        val btnCriar = findViewById<Button>(R.id.btnCriarAlbum)

        etTitulo.setText(albumNameFromIntent)

        findViewById<Button>(R.id.btnSelecionarCapa).setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnCriar.setOnClickListener {

            if (selectedCoverUri != null) {
                val githubManager = GitHubManager(
                    context = this,
                    token = token,
                    owner = userGit,
                    repo = repoGit
                )

                val pathCapa = "album_$albumId/cover.jpg"

                lifecycleScope.launch {
                    val sucesso = githubManager.uploadFile(selectedCoverUri!!, pathCapa)

                    if (sucesso) {
                        val urlCapa = "https://raw.githubusercontent.com/$userGit/$repoGit/main/$pathCapa"
                        salvarAlbum(albumId, etTitulo.text.toString(), etArtista.text.toString(), etGenero.text.toString(), etAno.text.toString().toInt(), urlCapa)
                    } else {
                        Toast.makeText(this@AlbumCreateActivity, "Erro ao subir capa para o GitHub", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                salvarAlbum(albumId, etTitulo.text.toString(), etArtista.text.toString(), etGenero.text.toString(), etAno.text.toString().toInt(), "")
            }

        }
    }

    private fun salvarAlbum(
        id: String,
        titulo: String,
        artista: String,
        genero: String,
        ano: Int,
        urlCapa: String
    ) {
        val album = Album(
            id = id,
            titulo = titulo,
            artist = artista,
            imageUrl = urlCapa,
            releaseYear = ano,
            userId = userId,
            genre = genero
        )

        firebaseService.salvarAlbumGlobal(album) { sucesso, erro ->
            if (sucesso) {
                Toast.makeText(this, "Álbum criado com sucesso!", Toast.LENGTH_SHORT).show()
                abrirUploadDeFaixas(album)
            } else {
                Toast.makeText(this, "Erro: $erro", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedCoverUri = uri
            findViewById<ImageView>(R.id.ivPreviewCapa).setImageURI(uri)
        }
    }


    private fun abrirUploadDeFaixas(album: Album) {
        val intent =  Intent(this, TrackUploadActivity::class.java)
        intent.putExtra("albumId", album.id)
        intent.putExtra("albumTitle", album.titulo)
        intent.putExtra("artistName", album.artist)
        intent.putExtra("genre", album.genre)
        startActivity(intent)
    }
}
