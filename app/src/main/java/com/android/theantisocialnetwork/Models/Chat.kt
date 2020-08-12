package com.android.theantisocialnetwork.Models

import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.message_alert_row.view.*
import kotlinx.android.synthetic.main.message_receiver_row_chatlog.view.*
import kotlinx.android.synthetic.main.message_sender_row_chatlog.view.*
import kotlinx.android.synthetic.main.post_row.view.*
import kotlinx.android.synthetic.main.user_row_latest_messages.view.*


//** File containing Chat classes for modeling.

class ChatSenderItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.textViewSender.text = text
    }
    override fun getLayout(): Int {
        return R.layout.message_sender_row_chatlog
    }
}

class ChatReceiverItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.textViewReceiver.text = text

        val uri = user.profileImageURL
        val targetImageView = viewHolder.itemView.imageViewReceiver
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.message_receiver_row_chatlog
    }
}

class PostReceiverItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.textViewPostContent.text = text
    }
    override fun getLayout(): Int {
        return R.layout.post_row
    }
}

class AlertReceiverItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.textViewAlert.text = text
    }
    override fun getLayout(): Int {
        return R.layout.message_alert_row
    }
}

class ChatMessage(val id: String, val text: String, val senderID: String, val receiverID: String, val timestamp: Long){
    constructor() : this("","","","",-1)
}

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewLatestMessages.text = chatMessage.text

        val chatPartnerID: String
        if (chatMessage.senderID == FirebaseAuth.getInstance().uid){
            chatPartnerID = chatMessage.receiverID
        } else {
            chatPartnerID = chatMessage.senderID
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerID")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.textViewLatestUsername.text = chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.imageViewLatestPhoto
                Picasso.get().load(chatPartnerUser?.profileImageURL).into(targetImageView)
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.user_row_latest_messages
    }
}
