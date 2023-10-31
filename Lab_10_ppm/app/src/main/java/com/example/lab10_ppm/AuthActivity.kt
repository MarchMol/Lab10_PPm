package com.example.lab10_ppm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


var signupButton: Button? = null
var loginButton: Button? = null
var googleButton: Button? = null
var emailText: EditText? = null
var passwordText: EditText? = null
var authLayout: LinearLayout? = null

private var GOOGLE_SIGN_IN =100
class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("Message","integración de firebase completa")
        analytics.logEvent("Initscreen",bundle)

        signupButton = findViewById<Button>(R.id.signUpButton)
        loginButton = findViewById<Button>(R.id.logInButton)
        emailText = findViewById<EditText>(R.id.emailEditText)
        passwordText = findViewById<EditText>(R.id.passwordEditText)
        authLayout = findViewById<LinearLayout>(R.id.authLayout)
        googleButton = findViewById<Button>(R.id.googleButton)

        setup()
        session()
    }

    override fun onStart() {
        super.onStart()
        authLayout?.visibility = View.VISIBLE
    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        val provider = prefs.getString("provider",null)

        if(email!=null && provider!= null){
            authLayout?.visibility = View.INVISIBLE
            showHome(email,ProviderType.valueOf(provider))
        }
    }

    fun setup(){
        title = "Autenticación"

        signupButton?.setOnClickListener{
            if(emailText?.text?.isNotEmpty()!! && passwordText?.text?.isNotEmpty()!!){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        emailText?.text.toString(), passwordText?.text.toString()
                    ).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?:"",ProviderType.BASIC)
                        } else{
                            showAlert()
                        }
                    }
            }
        }

        loginButton?.setOnClickListener{
            if(emailText?.text?.isNotEmpty()!! && passwordText?.text?.isNotEmpty()!!){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        emailText?.text.toString(), passwordText?.text.toString()
                    ).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?:"",ProviderType.BASIC)
                        } else{
                            showAlert()
                        }
                    }
            }
        }

        googleButton?.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("969143531124-u4me6pgp1c9s73klqls69vfvgk75579v.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== GOOGLE_SIGN_IN){
            Log.d("MENSAJE", "se entro: ")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                val account = task.getResult(ApiException::class.java)

                if(account!=null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if(it.isSuccessful){
                            showHome(account.email ?:"",ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }
                }
            } catch (e:ApiException){
                Log.d("ERROR DE CONECCIÓN", e.toString())
                showAlert()
            }
        }
    }



    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        val dialog:AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email:String, provider:ProviderType){
        val homeIntent = Intent(this,HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider.name)
        }
        startActivity(homeIntent)
    }
}
