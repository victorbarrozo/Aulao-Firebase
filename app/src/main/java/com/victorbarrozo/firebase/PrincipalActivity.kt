package com.victorbarrozo.firebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victorbarrozo.firebase.databinding.ActivityMainBinding
import com.victorbarrozo.firebase.databinding.ActivityPrincipalBinding

class PrincipalActivity : AppCompatActivity() {
    val binding by lazy{
        ActivityPrincipalBinding.inflate(layoutInflater)
    }
    val autenticar by lazy {
        FirebaseAuth.getInstance()
    }
    val db by lazy{
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        binding.btnDeslogar.setOnClickListener {
            deslogar()
        }
    }

    private fun deslogar() {
        autenticar.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }
}