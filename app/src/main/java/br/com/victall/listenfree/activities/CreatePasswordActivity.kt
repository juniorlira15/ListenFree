package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import br.com.victall.listenfree.databinding.ActivityCreatePasswordBinding

class CreatePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePasswordBinding
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    btnNext.isEnabled = !s.isNullOrEmpty() && s.length >= 8
                }
            })

            btnNext.setOnClickListener {
                val password = etPassword.text.toString()
                if (validatePassword(password)) {
                    navigateToGenderChoice(password)
                }
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        if (password.length < 8) {
            binding.tilPassword.error = "A senha deve ter pelo menos 8 caracteres"
            return false
        }
        return true
    }

    private fun navigateToGenderChoice(password: String) {
        val intent = Intent(this, GenderChoiceActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
        }
        startActivity(intent)
    }
} 