package br.com.victall.listenfree.github

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GitHubManager(
    private val context: Context,
    private val token: String,
    private val owner: String,
    private val repo: String
) {
    private val client: OkHttpClient
    private val baseUrl = "https://api.github.com/repos/$owner/$repo"

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun createFolder(folderPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val content = JSONObject().apply {
                put("message", "Criar pasta: $folderPath")
                put("content", "")
            }.toString()

            val request = Request.Builder()
                .url("$baseUrl/contents/$folderPath")
                .put(content.toRequestBody("application/json".toMediaTypeOrNull()))
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("GitHubManager", "Erro ao criar pasta: ${response.code} - ${response.message}")
                return@withContext false
            }
            true
        } catch (e: Exception) {
            Log.e("GitHubManager", "Erro ao criar pasta", e)
            false
        }
    }

    suspend fun deleteFolder(folderPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Primeiro, precisamos obter o SHA do arquivo
            val getRequest = Request.Builder()
                .url("$baseUrl/contents/$folderPath")
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val getResponse = client.newCall(getRequest).execute()
            if (!getResponse.isSuccessful) {
                Log.e("GitHubManager", "Erro ao obter SHA: ${getResponse.code} - ${getResponse.message}")
                return@withContext false
            }

            val jsonResponse = JSONObject(getResponse.body?.string())
            val sha = jsonResponse.getString("sha")

            val content = JSONObject().apply {
                put("message", "Deletar pasta: $folderPath")
                put("sha", sha)
            }.toString()

            val deleteRequest = Request.Builder()
                .url("$baseUrl/contents/$folderPath")
                .delete(content.toRequestBody("application/json".toMediaTypeOrNull()))
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(deleteRequest).execute()
            if (!response.isSuccessful) {
                Log.e("GitHubManager", "Erro ao deletar pasta: ${response.code} - ${response.message}")
                return@withContext false
            }
            true
        } catch (e: Exception) {
            Log.e("GitHubManager", "Erro ao deletar pasta", e)
            false
        }
    }

    suspend fun uploadFile(uri: Uri, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                ensureFolderExists(path)

                val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext false

                val fileName = context.contentResolver.getFileName(uri)
                val fullPath = listOf(path.trimEnd('/'), fileName).joinToString("/")

                val base64Content = Base64.encodeToString(content, Base64.NO_WRAP)
                val requestBody = """
                {
                    "message": "Upload de arquivo: $fileName",
                    "content": "$base64Content"
                }
            """.trimIndent()

                val request = Request.Builder()
                    .url("$baseUrl/contents/$fullPath")
                    .put(requestBody.toRequestBody("application/json".toMediaType()))
                    .header("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }


    private suspend fun ensureFolderExists(path: String) = withContext(Dispatchers.IO) {
        if (path.isEmpty()) return@withContext

        try {
            val contents = listContents(path)

            // Verifica se já existe algum item na pasta (ignorando .gitkeep)
            val realContentExists = contents.any { it != ".gitkeep" && it.isNotBlank() }

            if (!realContentExists) {
                val gitkeepContent = Base64.encodeToString("".toByteArray(), Base64.NO_WRAP)
                val requestBody = """
                {
                    "message": "Criando .gitkeep em $path",
                    "content": "$gitkeepContent"
                }
            """.trimIndent()

                val request = Request.Builder()
                    .url("$baseUrl/contents/${path.trimEnd('/')}/.gitkeep")
                    .put(requestBody.toRequestBody("application/json".toMediaType()))
                    .header("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.w("GitHubManager", "Falha ao criar .gitkeep: ${response.code}")
                } else {
                    Log.d("GitHubManager", ".gitkeep criado com sucesso em $path")
                }
            } else {
                Log.d("GitHubManager", "Pasta $path já possui conteúdo real. Nenhum .gitkeep necessário.")
            }
        } catch (e: Exception) {
            Log.e("GitHubManager", "Erro ao verificar/criar pasta", e)
        }
    }

    suspend fun listContents(path: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/contents/$path")
                .header("Authorization", "token $token")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("GitHubManager", "Erro ao listar conteúdo: ${response.code} - ${response.message}")
                return@withContext emptyList()
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                Log.d("GitHubManager", "Resposta vazia para o caminho: $path")
                return@withContext emptyList()
            }

            try {
                Log.d("GitHubManager", "Resposta do GitHub: $responseBody")
                val contents = mutableListOf<String>()
                
                // Tenta primeiro como array
                try {
                    val jsonArray = org.json.JSONArray(responseBody)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val name = item.getString("name")
                        // Filtra a própria pasta atual
                        if (name == path.substringAfterLast("/")) continue
                        val isDir = !name.contains(".")
                        val finalName = if (isDir) "$name/" else name
                        Log.d("GitHubManager", """
                            Item $i:
                            - Nome: $name
                            - É pasta: $isDir
                            - Nome final: $finalName
                            - JSON completo: ${item.toString()}
                        """.trimIndent())
                        contents.add(finalName)
                    }
                } catch (e: org.json.JSONException) {
                    // Se falhar, tenta como objeto único
                    val item = org.json.JSONObject(responseBody)
                    val name = item.getString("name")
                    val isDir = !name.contains(".")
                    val finalName = if (isDir) "$name/" else name
                    Log.d("GitHubManager", """
                        Item único:
                        - Nome: $name
                        - É pasta: $isDir
                        - Nome final: $finalName
                        - JSON completo: ${item.toString()}
                    """.trimIndent())
                    contents.add(finalName)
                }
                
                contents.sortWith(compareBy(
                    { !it.endsWith("/") },
                    { it.lowercase() }
                ))
                
                Log.d("GitHubManager", "Lista final ordenada: $contents")
                contents
            } catch (e: Exception) {
                Log.e("GitHubManager", "Erro ao processar resposta JSON", e)
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("GitHubManager", "Erro ao listar conteúdo", e)
            emptyList()
        }
    }

    suspend fun uploadMultipleFiles(uris: List<Uri>, path: String, onProgress: (Int, Int, String) -> Unit): Pair<Int, Int> {
        return withContext(Dispatchers.IO) {
            var successCount = 0
            var failCount = 0
            val totalFiles = uris.size

            ensureFolderExists(path)

            uris.forEachIndexed { index, uri ->
                try {
                    val fileName = context.contentResolver.getFileName(uri)
                    val filePath = listOf(path.trimEnd('/'), fileName).joinToString("/")

                    val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw Exception("Não foi possível ler o arquivo")

                    val base64Content = Base64.encodeToString(content, Base64.NO_WRAP)
                    val requestBody = """
                    {
                        "message": "Upload de arquivo: $fileName",
                        "content": "$base64Content"
                    }
                """.trimIndent()

                    val request = Request.Builder()
                        .url("$baseUrl/contents/$filePath")
                        .put(requestBody.toRequestBody("application/json".toMediaType()))
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        successCount++
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("GitHubManager", "Erro ao fazer upload de $fileName: ${response.code} - $errorBody")
                        failCount++
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.e("GitHubManager", "Erro ao processar arquivo", e)
                }

                onProgress(index + 1, totalFiles, context.contentResolver.getFileName(uri))
            }

            Pair(successCount, failCount)
        }
    }


    private fun android.content.ContentResolver.getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "arquivo_${System.currentTimeMillis()}"
    }
} 