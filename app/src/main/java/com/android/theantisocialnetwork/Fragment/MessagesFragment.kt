package com.android.theantisocialnetwork.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.theantisocialnetwork.Models.ChatMessage
import com.android.theantisocialnetwork.Models.LatestMessageRow
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_messages.*

class MessagesFragment : Fragment() {

    val latestMessageMap = HashMap<String, ChatMessage>()
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_messages, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerViewLatestMessages.adapter = adapter

        listenForLatestMessages()

        adapter.setOnItemClickListener { item, _ ->
            val newFragment: Fragment = ChatLogFragment()
            val bundle = Bundle()
            val row = item as LatestMessageRow
            row.chatPartnerUser
            bundle.putParcelable( "key", row.chatPartnerUser)
            newFragment.setArguments(bundle)

            val transaction = fragmentManager!!.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.containerOfFragments, newFragment) // give your fragment container id in first parameter
            transaction.addToBackStack(null) // if written, this transaction will be added to backstack
            transaction.commit()
        }
    }


    private fun refreshRecylerViewMessages(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val senderID = FirebaseAuth.getInstance().uid
        val ref  = FirebaseDatabase.getInstance().getReference("/latest-messages/$senderID")


        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                adapter.add(LatestMessageRow(chatMessage))
                latestMessageMap[p0.key!!] = chatMessage
                refreshRecylerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[p0.key!!] = chatMessage
                refreshRecylerViewMessages()
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
