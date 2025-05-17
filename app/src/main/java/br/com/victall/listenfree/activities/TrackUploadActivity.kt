package br.com.victall.listenfree.activities

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.victall.listenfree.R
import br.com.victall.listenfree.core.services.FirebaseService
import br.com.victall.listenfree.github.GitHubManager
import br.com.victall.listenfree.models.Track
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackUploadActivity : AppCompatActivity() {

    private lateinit var firebaseService: FirebaseService
    private lateinit var githubManager: GitHubManager

    private lateinit var albumId: String
    private lateinit var albumTitle: String
    private lateinit var artistName: String
    private lateinit var genre: String
    private lateinit var userId: String

    private lateinit var progresso: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var btnVoltar: Button

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            uploadMusicas(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_upload)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        albumId = intent.getStringExtra("albumId").orEmpty()
        albumTitle = intent.getStringExtra("albumTitle").orEmpty()
        artistName = intent.getStringExtra("artistName").orEmpty()
        genre = intent.getStringExtra("genre").orEmpty()
        userId = "uid123" // simulado

        firebaseService = FirebaseService()
        githubManager = GitHubManager(
            context = this,
            token = "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",
            owner = "juniorlira16",
            repo = "listenfree-assets"
        )

        findViewById<TextView>(R.id.tvTituloAlbum).text = "Álbum: $albumTitle"
        findViewById<Button>(R.id.btnSelecionarMusicas).setOnClickListener {
            filePicker.launch("audio/*")
        }

        btnVoltar = findViewById(R.id.btnVoltarRepos)
        btnVoltar.setOnClickListener { finish() }

        progresso = findViewById(R.id.progressoUpload)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun uploadMusicas(uris: List<Uri>) {
        progresso.visibility = View.VISIBLE
        tvStatus.text = "Preparando envio..."

        lifecycleScope.launch {
            val (sucesso, falha) = githubManager.uploadMultipleFiles(uris, albumId) { atual, total, nome ->
                runOnUiThread {
                    tvStatus.text = "Enviando $atual de $total: $nome"
                    progresso.progress = (atual * 100) / total
                }
            }

            uris.forEach { uri ->
                val nome = contentResolver.getFileName(uri)
                val duration = getAudioDuration(uri)
                val trackId = "track_" + System.currentTimeMillis()
                val trackUrl = "https://raw.githubusercontent.com/juniorlira16/listenfree-assets/main/$albumId/$nome"

                val track = Track(
                    id = trackId,
                    name = nome.removeSuffix(".mp3"),
                    albumId = albumId,
                    artistName = artistName,
                    duration = duration,
                    audioUrl = trackUrl,
                    userId = userId,
                    genre = genre
                )

                firebaseService.salvarTrackGlobal(track) { _, _ -> }
            }

            withContext(Dispatchers.Main) {
                progresso.visibility = View.GONE
                showResultDialog(sucesso, falha)
            }
        }
    }

    private fun showResultDialog(success: Int, fail: Int) {
        val message = buildString {
            append("\uD83C\uDFB5 Upload finalizado!\n\n")
            append("✅ $success arquivo(s) enviado(s) com sucesso\n")
            append("❌ $fail falha(s) durante envio")
        }

        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ListenFree_Spotify_Dialog)
            .setTitle("Resultado do Envio")
            .setMessage(message)
            .setPositiveButton("Voltar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun getAudioDuration(uri: Uri): Int {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(this, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            (durationStr?.toLongOrNull() ?: 0L).div(1000).toInt()
        } catch (e: Exception) {
            0
        } finally {
            mmr.release()
        }
    }

    private fun ContentResolver.getFileName(uri: Uri): String {
        var name: String? = null
        query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
        return name ?: "musica_${System.currentTimeMillis()}.mp3"
    }
} 
