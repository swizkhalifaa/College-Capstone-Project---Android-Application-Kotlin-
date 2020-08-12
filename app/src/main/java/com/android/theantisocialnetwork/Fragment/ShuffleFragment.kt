package com.android.theantisocialnetwork.Fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.theantisocialnetwork.MainActivity
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
import com.thekhaeng.pushdownanim.PushDownAnim
import com.thekhaeng.pushdownanim.PushDownAnim.MODE_SCALE
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_shuffle.*


class ShuffleFragment : Fragment() {

    var didFetchPost = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_shuffle, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(!didFetchPost){
            fetchPosts()
            didFetchPost = true
        }
        else moveToNext(recyclerViewPosts)
        // Button animations
        buttonShuffleReject.setOnClickListener {
            moveToNext(recyclerViewPosts)
        }
        buttonShuffleReject.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                PushDownAnim.setPushDownAnimTo(v)
                    .setScale( MODE_SCALE, 0.93f  )
                return@OnTouchListener true
            }
            false
        })

        buttonShuffleEngage.setOnClickListener {
            engagePost()
        }
        buttonShuffleEngage.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                PushDownAnim.setPushDownAnimTo(v)
                    .setScale( MODE_SCALE, 0.93f  )
                return@OnTouchListener true
            }
            false
        })
    }


    private val timeOut:Long = 400
    private val adapter = GroupAdapter<GroupieViewHolder>()
    var currentIndex = 0
    private val postBank = mutableListOf<UserPostItem>(
    )

    private fun moveToNext(recyclerViewPosts: RecyclerView)
    {
        Log.d("Shuffle", currentIndex.toString())
        currentIndex = (currentIndex + 1) % postBank.size
        adapter.clear()
        // Post Enter and Exit animations
        val exitLeft = AnimationUtils.loadAnimation(context,
            R.anim.exit_to_left)
        val enterRight = AnimationUtils.loadAnimation(context,
            R.anim.enter_from_right)
        recyclerViewPosts.startAnimation(exitLeft)
        Handler().postDelayed({
            recyclerViewPosts.startAnimation(enterRight)
        },timeOut)

        displayCurrentPost(recyclerViewPosts)
    }

    private fun displayCurrentPost(recyclerViewPosts: RecyclerView){
        adapter.add(postBank[currentIndex])
        recyclerViewPosts.adapter = adapter
    }

    private fun fetchPosts(){
        var firstEntry = true
        val thisUser = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-posts")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{

                    val post = it.getValue(UserPost::class.java)

                    if (thisUser != post?.senderID && post != null){
                        postBank.add(UserPostItem(post))
                    }
                    else {
                        Log.d("Shuffle", "Not suitable post")
                    }
                }
                Log.d("Shuffle", "Bank size: " + postBank.size.toString())
                if (firstEntry) {
                    firstEntry = false
                    displayCurrentPost(recyclerViewPosts)
                }

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun engagePost(){
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {

                        val senderID = FirebaseAuth.getInstance().uid
                        val item = adapter.getItem(0)
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
                        val notificationUser = User(MainActivity.currentUser!!.uid, MainActivity.currentUser!!.username, MainActivity.currentUser!!.profileImageURL)

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

}