package com.android.theantisocialnetwork.Fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.theantisocialnetwork.MainActivity.Companion.currentUser
import com.android.theantisocialnetwork.Models.ChatMessage
import com.android.theantisocialnetwork.Models.User
import com.android.theantisocialnetwork.Models.UserPost
import com.android.theantisocialnetwork.Models.UserPostItem
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_hashtag.*


class HashtagFragment : Fragment() {

    // Keyboard hiding functions
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_hashtag, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonHashtagSearch.setOnClickListener {
            hideKeyboard()

            fetchPosts(recyclerViewPosts)
        }
    }


    private fun fetchPosts(recyclerViewPosts: RecyclerView){

        var matchFound = false
        val thisUser = FirebaseAuth.getInstance().uid
        // Searched hashtag is given $ cap to indicate the end of text
        val hashtagSearched = ("#".plus(txtbxHashtagSearch.text.toString())).plus("$").toRegex()
        Log.d("Hashtag", hashtagSearched.toString())
        val adapter = GroupAdapter<GroupieViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/user-posts")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{

                    val post = it.getValue(UserPost::class.java)
                    val text = post!!.text
                    // Finding the hashtag searched within text of post
                    val matchResult = hashtagSearched.containsMatchIn(text)

                    if (matchResult && thisUser != post.senderID){
                        Log.d("Hashtag", "match found")
                        matchFound = true
                        adapter.add(UserPostItem(post))
                    }
                    else {
                        Log.d("Hashtag", "no match found")
                    }
                }
                if(matchFound) textViewNoMatch.text = ""
                else textViewNoMatch.text = "No Match Found :("

                adapter.setOnItemClickListener {item, _ ->
                    val dialogClickListener =
                        DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {

                                    val senderID = FirebaseAuth.getInstance().uid
                                    val userpostItem = item as UserPostItem
                                    val newFragment: Fragment = ChatLogFragment()
                                    val bundle = Bundle()
                                    bundle.putParcelable( "key", userpostItem.chatPartnerUser)
                                    newFragment.arguments = bundle
                                    // Uploading post and alert to the desired chat
                                    val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$senderID/${userpostItem.chatPartnerUser?.uid}").push()
                                    val receiverReference = FirebaseDatabase.getInstance().getReference("/user-messages/${userpostItem.chatPartnerUser?.uid}/$senderID").push()
                                    val notificationReference = FirebaseDatabase.getInstance().getReference("/user-notifications/${userpostItem.chatPartnerUser?.uid}/$senderID")

                                    val alertreference = FirebaseDatabase.getInstance().getReference("/user-messages/$senderID/${userpostItem.chatPartnerUser?.uid}").push()
                                    val alertreceiverReference = FirebaseDatabase.getInstance().getReference("/user-messages/${userpostItem.chatPartnerUser?.uid}/$senderID").push()

                                    val userPost = ChatMessage(reference.key!!, userpostItem.userpost.text, "Post", "Post", System.currentTimeMillis() / 1000)
                                    val alertPost = ChatMessage(reference.key!!, "You are connected to discuss this post, be nice!", "Alert", "Alert", System.currentTimeMillis() / 1000)
                                    val notificationUser = User(currentUser!!.uid,currentUser!!.username, currentUser!!.profileImageURL)

                                    reference.setValue(userPost)
                                    receiverReference.setValue(userPost)

                                    alertreference.setValue(alertPost)
                                    alertreceiverReference.setValue(alertPost)

                                    notificationReference.setValue(notificationUser)

                                    val transaction = fragmentManager!!.beginTransaction()
                                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    transaction.replace(R.id.containerOfFragments, newFragment) // give your fragment container id in first parameter
                                    transaction.addToBackStack(null) // if written, this transaction will be added to backstack
                                    transaction.commit()

                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                }
                            }
                        }
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setMessage("Engage with this post?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show()
                }
                recyclerViewPosts.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}