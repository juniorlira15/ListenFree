package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.victall.listenfree.databinding.ActivityCadastroEmailBinding

class CadastroEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCadastroEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupViews()
    }
    
    private fun setupViews() {
        binding.apply {
            // Configura o botão de voltar
//            btnBack.setOnClickListener {
//                finish()
//            }
            
            // Configura o campo de email
            etEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    btnNext.isEnabled = !s.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
                }
            })
            
            // Configura o botão de próximo
            btnNext.setOnClickListener {
                val email = etEmail.text.toString()
                if (validateEmail(email)) {
                    navigateToCreatePassword(email)
                }
            }
        }
    }
    
    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            binding.tilEmail.error = "O email não pode estar vazio"
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email inválido"
            return false
        }
        
        return true
    }
    
    private fun navigateToCreatePassword(email: String) {
        val intent = Intent(this, CreatePasswordActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(intent)
    }
}