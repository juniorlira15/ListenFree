package br.com.victall.listenfree.github

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.victall.listenfree.R
import br.com.victall.listenfree.activities.AlbumCreateActivity
import br.com.victall.listenfree.databinding.ActivityGithubRepositoryBinding
import br.com.victall.listenfree.databinding.DialogCreateAlbumBinding
import kotlinx.coroutines.launch
import java.io.File

class GitHubRepositoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGithubRepositoryBinding
    private lateinit var githubManager: GitHubManager
    private lateinit var adapter: RepositoryContentAdapter
    private var currentPath = ""
    private var selectedFileUris: List<Uri> = emptyList()
    private var uploadDialog: AlertDialog? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedFileUris = uris
            handleFileUploads(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGithubRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Substituir com suas credenciais do GitHub
        githubManager = GitHubManager(
            context = this,
            token = "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",
            owner = "juniorlira16",
            repo = "listenfree-assets"
        )

        // Simule o ID do usuário Firebase
        val idUsuarioFirebase = "fakeUserId123"
        currentPath = idUsuarioFirebase


        setupRecyclerView()
        setupUI()
        loadContents()
    }

    private fun setupRecyclerView() {
        adapter = RepositoryContentAdapter(
            onItemClick = { item ->
                if (item.endsWith("/")) {
                    val folderName = item.removeSuffix("/")
                    val nextPath = if (currentPath.isEmpty()) folderName else "$currentPath/$folderName"
                    if (nextPath != currentPath) {
                        currentPath = nextPath
                        loadContents()
                    }
                }
            },
            onDeleteClick = { item ->
                showDeleteConfirmationDialog(item)
            },
            onUploadClick = { item ->
                if (hasStoragePermission()) {
                    val folderName = item.removeSuffix("/")
                    currentPath = if (currentPath.isEmpty()) folderName else "$currentPath/$folderName"
                    openFilePicker()
                } else {
                    requestStoragePermission()
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GitHubRepositoryActivity)
            adapter = this@GitHubRepositoryActivity.adapter
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            if (currentPath.isNotEmpty()) {
                currentPath = currentPath.substringBeforeLast("/", "")
                loadContents()
            } else {
                finish()
            }
        }

        binding.btnCreateAlbum.setOnClickListener {
            showCreateAlbumDialog()
        }

        binding.btnUploadMusic.setOnClickListener {
            if (hasStoragePermission()) {
                openFilePicker()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun showCreateAlbumDialog() {
        val dialogBinding = DialogCreateAlbumBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Criar Novo Álbum")
            .setView(dialogBinding.root)
            .setPositiveButton("Criar") { _, _ ->
                val albumName = dialogBinding.etAlbumName.text.toString().trim()
                if (albumName.isNotEmpty()) {
                    val albumId = "album_" + System.currentTimeMillis()

                    val intent = Intent(this, AlbumCreateActivity::class.java).apply {
                        putExtra("albumId", albumId)
                        putExtra("albumName", albumName)
                        putExtra("basePath", currentPath) // opcional se quiser usar no GitHub
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun loadContents() {
        binding.progressBar.visibility = View.VISIBLE
        updateTitle()
        lifecycleScope.launch {
            try {
                val contents = githubManager.listContents(currentPath)
                if (contents.isEmpty()) {
                    Toast.makeText(
                        this@GitHubRepositoryActivity,
                        "Pasta vazia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                adapter.submitList(contents)
            } catch (e: Exception) {
                Toast.makeText(
                    this@GitHubRepositoryActivity,
                    "Erro ao carregar conteúdo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Em caso de erro, volta para a pasta anterior
                if (currentPath.isNotEmpty()) {
                    currentPath = currentPath.substringBeforeLast("/", "")
                    loadContents()
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateTitle() {
        val title = if (currentPath.isEmpty()) {
            "Gerenciar Repositório"
        } else {
            "Você está em: /$currentPath"
        }
        binding.tvTitle.text = title
    }

    private fun createFolder(folderName: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val path = if (currentPath.isEmpty()) folderName else "$currentPath/$folderName"
                val success = githubManager.createFolder(path)
                if (success) {
                    Toast.makeText(
                        this@GitHubRepositoryActivity,
                        "Pasta criada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadContents()
                } else {
                    Toast.makeText(
                        this@GitHubRepositoryActivity,
                        "Erro ao criar pasta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@GitHubRepositoryActivity,
                    "Erro ao criar pasta: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(item: String) {
        AlertDialog.Builder(androidx.appcompat.view.ContextThemeWrapper(this, R.style.ThemeOverlay_ListenFree_Spotify_Dialog))
            .setTitle("Deletar Pasta")
            .setMessage("Tem certeza que deseja deletar a pasta '$item'?")
            .setPositiveButton("Deletar") { _, _ ->
                deleteFolder(item)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteFolder(folderName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val newFolderName = folderName.replace("/","")
        lifecycleScope.launch {
            try {
                val path = if (currentPath.isEmpty()) newFolderName else "$currentPath/$newFolderName"
                val success = githubManager.deleteFolder(path)
                if (success) {
                    Toast.makeText(
                        this@GitHubRepositoryActivity,
                        "Pasta deletada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadContents()
                } else {
                    Toast.makeText(
                        this@GitHubRepositoryActivity,
                        "Erro ao deletar pasta",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@GitHubRepositoryActivity,
                    "Erro ao deletar pasta: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                STORAGE_PERMISSION_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("audio/mpeg")
    }

    private fun handleFileUploads(uris: List<Uri>) {
        val progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.dialog_upload_progress)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val tvProgress = progressDialog.findViewById<TextView>(R.id.tvProgress)
        val progressBar = progressDialog.findViewById<ProgressBar>(R.id.progressBar)

        lifecycleScope.launch {
            try {
                val (successCount, failCount) = githubManager.uploadMultipleFiles(uris, currentPath) { current, total, fileName ->
                    runOnUiThread {
                        tvProgress.text = "Enviando $current de $total arquivos\n$fileName"
                        progressBar.progress = (current * 100) / total
                    }
                }

                progressDialog.dismiss()
                val message = when {
                    successCount > 0 && failCount > 0 -> "$successCount arquivo(s) enviado(s) com sucesso\n$failCount arquivo(s) falharam"
                    successCount > 0 -> "$successCount arquivo(s) enviado(s) com sucesso"
                    else -> "Falha ao enviar $failCount arquivo(s)"
                }
                Toast.makeText(this@GitHubRepositoryActivity, message, Toast.LENGTH_LONG).show()
                loadContents()
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(
                    this@GitHubRepositoryActivity,
                    "Erro ao enviar arquivos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: "arquivo_${System.currentTimeMillis()}"
    }

    private fun copyFileToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = getFileName(uri)
            val file = File(cacheDir, fileName)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker()
            } else {
                Toast.makeText(
                    this,
                    "Permissão necessária para acessar os arquivos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }
} 