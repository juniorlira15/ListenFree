package br.com.victall.listenfree.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import br.com.victall.listenfree.databinding.ActivityGenderChoiceBinding

class GenderChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderChoiceBinding
    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            rgGender.setOnCheckedChangeListener { group, checkedId ->
                btnNext.isEnabled = true
            }

            btnNext.setOnClickListener {
                val selectedGender = when (rgGender.checkedRadioButtonId) {
                    rbMale.id -> "male"
                    rbFemale.id -> "female"
                    rbNonBinary.id -> "non_binary"
                    rbOther.id -> "other"
                    rbPreferNotToSay.id -> "prefer_not_to_say"
                    else -> null
                }

                if (selectedGender != null) {
                    navigateToName(selectedGender)
                }
            }
        }
    }

    private fun navigateToName(gender: String) {
        val intent = Intent(this, NameActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
            putExtra("gender", gender)
        }
        startActivity(intent)
    }
} 