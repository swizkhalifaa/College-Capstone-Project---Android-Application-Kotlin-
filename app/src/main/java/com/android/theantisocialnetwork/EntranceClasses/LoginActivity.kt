package com.android.theantisocialnetwork.EntranceClasses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.theantisocialnetwork.MainActivity
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        backToRegistrationtxtView.setOnClickListener {
            finish()
        }

        buttonLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin(){

        val email =  txtbxLoginEmail.text.toString()
        val password = txtbxLoginPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter email or password", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase method which handles email and password authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                else {
                    Log.d("Login", "Successfully signed in ${it.result?.user?.email} with uid: ${it.result?.user?.uid}")
                    // Intent loads user into the main activity of the application
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                }
            }
            .addOnFailureListener {
                Log.d("Login","Failed to sign in user: ${it.message}")
                Toast.makeText(this,"Failed to sign in: ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
}
