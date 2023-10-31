package com.example.lab10_ppm

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

var cerrarButton: Button? = null
var emailTextHome: TextView? = null
var providerTextHome: TextView? = null

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        cerrarButton = findViewById<Button>(R.id.cerrarSesionButton)
        emailTextHome = findViewById<TextView>(R.id.emailTextView)
        providerTextHome = findViewById<TextView>(R.id.providerTextView)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        setup(email ?:"",provider ?:"")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

    }

    private fun setup(email:String,provider:String){
        title = "Inicio"
        emailTextHome?.text = email
        providerTextHome?.text = provider

        cerrarButton?.setOnClickListener{
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}