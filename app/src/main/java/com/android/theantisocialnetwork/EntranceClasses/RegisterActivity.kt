package com.android.theantisocialnetwork.EntranceClasses

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.theantisocialnetwork.MainActivity
import com.android.theantisocialnetwork.Models.User
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonRegister.setOnClickListener {
            performRegister()
        }

        textViewAlreadyHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonRegisterSelectPhoto.setOnClickListener {
            Log.d("RegisterActivity","User selecting photo externally")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    // Photo uri declared within class to be used by multiple functions
    var selectPhotoUri: Uri? = null

    // Handling photo data obtained from implicit intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "Photo was selected")
            selectPhotoUri = data.data

            // Display image within bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPhotoUri)
            selectPhotoImageView.setImageBitmap(bitmap)
            buttonRegisterSelectPhoto.alpha = 0f
        }
    }

    private fun performRegister(){

        if(selectPhotoUri == null ) {
            Toast.makeText(this,"Please upload a picture in the circle above",Toast.LENGTH_SHORT).show()
            return
        }

        val username = txtbxRegisterUsername.text.toString()
        val email =  txtbxRegisterEmail.text.toString()
        val password = txtbxRegisterPassword.text.toString()

        if (username.length < 6 || username.length > 15){
            Toast.makeText(this,"Please enter a username between 6-15 characters",Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter email or password",Toast.LENGTH_SHORT).show()
            return
        }
        // Firebase method which handles email and password authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                else {
                    Log.d(
                        "RegisterActivity",
                        "Successfully made user with uid: ${it.result?.user?.uid}, awaiting photo validation"
                    )
                    uploadImageToFirebase()
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to create user: ${it.message}")
                Toast.makeText(this,"Failed to create user: ${it.message}",Toast.LENGTH_LONG).show()
            }
    }


    private fun uploadImageToFirebase(){
        if(selectPhotoUri == null) {
            Toast.makeText(this,"Please upload a profile picture",Toast.LENGTH_SHORT).show()
            return
        }
        // Creating firebase storage path
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Photo uploaded to path: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity", "File Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
                    .addOnFailureListener {
                        Log.d("RegisterActivity","URL download failure")
                        Toast.makeText(this,"Photo Error: ${it.message}",Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Photo failed to upload to a path")
                Toast.makeText(this,"Photo Error: ${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    private  fun saveUserToFirebaseDatabase(profileImageURL: String){

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            txtbxRegisterUsername.text.toString(),
            profileImageURL
        )

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","The user is now on firebase database")

                Toast.makeText(this,"User Created Successfully!",Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Failed to upload user info to database")
                Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_LONG).show()
            }
    }
}



