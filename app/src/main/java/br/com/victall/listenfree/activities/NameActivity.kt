package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import br.com.victall.listenfree.databinding.ActivityNameBinding

class NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding
    private var email: String? = null
    private var password: String? = null
    private var gender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")
        gender = intent.getStringExtra("gender")
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            // Habilita os links clicáveis
            tvTermsLink.movementMethod = LinkMovementMethod.getInstance()
            tvPrivacyLink.movementMethod = LinkMovementMethod.getInstance()

            etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    btnCreateAccount.isEnabled = !s.isNullOrEmpty()
                }
            })

            btnCreateAccount.setOnClickListener {
                val name = etName.text.toString()
                if (validateName(name)) {
                    navigateToArtistPreferred(name)
                }
            }

            // Configuração dos links
            tvTermsLink.setOnClickListener {
                // Implementar navegação para os Termos de Uso
            }

            tvPrivacyLink.setOnClickListener {
                // Implementar navegação para a Política de Privacidade
            }
        }
    }

    private fun validateName(name: String): Boolean {
        if (name.isBlank()) {
            binding.tilName.error = "O nome não pode estar vazio"
            return false
        }
        return true
    }

    private fun navigateToArtistPreferred(name: String) {
        val intent = Intent(this, ArtistPreferredActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
            putExtra("gender", gender)
            putExtra("name", name)
        }
        startActivity(intent)
    }
} 