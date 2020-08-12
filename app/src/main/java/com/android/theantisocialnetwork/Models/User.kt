package com.android.theantisocialnetwork.Models

import android.os.Parcelable
import com.android.theantisocialnetwork.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.post_row.view.*
import kotlinx.android.synthetic.main.user_row_notifications.view.*

//** File containing User classes for modeling.

@Parcelize
class User(val uid: String, val username: String, val profileImageURL: String): Parcelable{
    constructor() : this("","","")
}

class UserItem(val user: User): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewUsernameRow.text = user.username
        Picasso.get().load((user.profileImageURL)).into(viewHolder.itemView.imageViewPhotoRow)
    }
    override fun getLayout(): Int {
        return R.layout.user_row_notifications
    }
}

@Parcelize
class UserPost(val id: String, val text: String, val senderID: String?, val timestamp: Long): Parcelable{
    constructor() : this("","","",-1)
}

class UserPostItem(val userpost: UserPost) : Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewPostContent.text = userpost.text
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    val user = it.getValue(User::class.java)
                    if(user?.uid == userpost.senderID){
                        chatPartnerUser = user
                        return
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
    override fun getLayout(): Int {
        return R.layout.post_row
    }
}










