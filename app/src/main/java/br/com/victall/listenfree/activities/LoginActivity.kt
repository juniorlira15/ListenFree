package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.com.victall.listenfree.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSignIn.setOnClickListener(){
            val intent = Intent(this, CadastroEmailActivity::class.java)
            startActivity(intent)
        }

        setupViews()
    }
    
    private fun setupViews() {
        binding.apply {
            // Configura o botão de login
            btnSignIn.setOnClickListener {
                // TODO: Implementar lógica de login
                // Por enquanto, vamos apenas navegar para a tela principal
                //navigateToMain()
            }
            
            // Configura o botão de cadastro
            btnSignInWithGoogle.setOnClickListener {
                navigateToCadastroEmail()
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToCadastroEmail() {
        val intent = Intent(this, CadastroEmailActivity::class.java)
        startActivity(intent)
    }
}