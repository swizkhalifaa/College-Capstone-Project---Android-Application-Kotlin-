package com.android.theantisocialnetwork.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.theantisocialnetwork.Models.*
import com.android.theantisocialnetwork.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chatlog.*

class ChatLogFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    var receiverUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_chatlog, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recylclerViewChatlog.adapter = adapter

        buttonBackChatlog.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        buttonChatlogSendMessage.setOnClickListener {
            performSendMessage()
        }
    }


    override fun onResume() {

        dressingChatLogUI()

        listenForMessages(recylclerViewChatlog)

        layoutChatlog.visibility = View.VISIBLE
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        (activity as AppCompatActivity?)!!.textViewTitleMain.isVisible = false
        // Hiding main navigation bar
        val navBar: BottomNavigationView = activity!!.findViewById(R.id.bottomNavigationView)
        navBar.visibility = View.GONE
    }
    override fun onStop() {

        layoutChatlog.visibility = View.GONE
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        (activity as AppCompatActivity?)!!.textViewTitleMain.isVisible = true
        toolbarChatlog.visibility = View.GONE
        val navBar: BottomNavigationView = activity!!.findViewById(R.id.bottomNavigationView)
        navBar.visibility = View.VISIBLE
    }
    private  fun dressingChatLogUI(){

        toolbarChatlog.visibility = View.VISIBLE
        // Receiving desired user information from bundle
        val bundle = this.arguments
        receiverUser = bundle?.getParcelable<User>("key") as User
        val username = receiverUser?.username
        textViewChatlogToolbar.text = username
    }


    private fun listenForMessages(recyclerViewChatlog: RecyclerView){

        val senderID = FirebaseAuth.getInstance().uid
        val receiverID = receiverUser?.uid
        val ref  = FirebaseDatabase.getInstance().getReference("/user-messages/$senderID/$receiverID")

        // Performs actions in the event of data additions at this specific reference in firebase
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null){
                    if(chatMessage.senderID == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatSenderItem(chatMessage.text))
                    }
                    // Handling posts within chat
                    else if(chatMessage.senderID == "Post"){
                        adapter.add(PostReceiverItem(chatMessage.text))
                    }
                    // Handling Alerts within chat
                    else if(chatMessage.senderID == "Alert"){
                        adapter.add(AlertReceiverItem(chatMessage.text))
                    }
                    else{
                        adapter.add(ChatReceiverItem(chatMessage.text, receiverUser!!))
                    }
                    recyclerViewChatlog.scrollToPosition(adapter.itemCount - 1)
                }
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun performSendMessage(){

        val text = editTextChatlog.text.toString()
        val senderID = FirebaseAuth.getInstance().uid
        val bundle = this.arguments
        val user = bundle?.getParcelable<User>("key") as User
        val receiverID = user.uid

        if (senderID == null || text.isBlank()) return
        // Establish firebase chat references for both user perspectives
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$senderID/$receiverID").push()
        val receiverReference = FirebaseDatabase.getInstance().getReference("/user-messages/$receiverID/$senderID").push()
        val chatMessage = ChatMessage(reference.key!!, text, senderID, receiverID, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("Chatlog","Saved the chat message: ${reference.key}")
                editTextChatlog.text.clear()
                recylclerViewChatlog.scrollToPosition(adapter.itemCount - 1)
            }
        // Storing message in both locations referenced above
        receiverReference.setValue(chatMessage)
        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderID/$receiverID")
        latestMessageRef.setValue(chatMessage)

        val receiverLatestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$receiverID/$senderID")
        receiverLatestMessageRef.setValue(chatMessage)
    }

}