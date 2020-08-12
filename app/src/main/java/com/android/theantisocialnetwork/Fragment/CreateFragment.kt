package com.android.theantisocialnetwork.Fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.theantisocialnetwork.Models.UserPost
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_create.*


class CreateFragment : Fragment() {

    // Keyboard hiding functions
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_create, container, false)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val postText = txtbxPostBox
        val createTextView = textViewCharCount

        // Character limit tracker
        createTextView.text = 250.toString()
        val mTitleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(postText.text.toString().trim().length < 251){
                    createTextView.text = (250 - postText.text.toString().trim().length).toString()
                }
                else{
                    createTextView.text = "0"
                }
            }
            override fun afterTextChanged(s: Editable) {}
        }
        postText.addTextChangedListener(mTitleTextWatcher)

        buttonCreatePost.setOnClickListener {
            hideKeyboard()

            performPost()
        }
    }
    private fun performPost(){

        val postText = txtbxPostBox
        val text = txtbxPostBox.text.toString()
        val senderID = FirebaseAuth.getInstance().uid
        if (text.isBlank()) {
            Toast.makeText(context,"Type something!", Toast.LENGTH_SHORT).show()
            return
        }
        val reference = FirebaseDatabase.getInstance().getReference("/user-posts").push()
        val userPost = UserPost(reference.key!!, text, senderID, System.currentTimeMillis() / 1000)
        reference.setValue(userPost)
            .addOnSuccessListener {
                postText.text.clear()
                Log.d("Create","Saved our post: ${reference.key}")
                Toast.makeText(context, "Posted!", Toast.LENGTH_SHORT).show()
            }
    }

}