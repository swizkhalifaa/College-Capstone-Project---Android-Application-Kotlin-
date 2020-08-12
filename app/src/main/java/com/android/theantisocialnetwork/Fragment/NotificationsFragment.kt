package com.android.theantisocialnetwork.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.android.theantisocialnetwork.Models.User
import com.android.theantisocialnetwork.Models.UserItem
import com.android.theantisocialnetwork.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    val thisUser = FirebaseAuth.getInstance().uid
    val ref = FirebaseDatabase.getInstance().getReference("/user-notifications/$thisUser")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recylclerViewNotifcations.adapter = adapter
        listenForLatestNotifications()

        adapter.setOnItemClickListener {item, _ ->
            val newFragment: Fragment = ChatLogFragment()
            val bundle = Bundle()
            val user = item as UserItem
            bundle.putParcelable( "key", user.user)
            newFragment.setArguments(bundle)
            Log.d("Notification",user.user.uid)

            val transaction = fragmentManager!!.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.containerOfFragments, newFragment) // give your fragment container id in first parameter
            transaction.addToBackStack(null) // if written, this transaction will be added to backstack
            transaction.commit()
            // Removal of notification from Firebase
            ref.child("${user.user.uid}").removeValue()
        }

        buttonBackNotifications.setOnClickListener{
            fragmentManager?.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        (activity as AppCompatActivity?)!!.textViewTitleMain.isVisible = false
    }
    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        (activity as AppCompatActivity?)!!.textViewTitleMain.isVisible = true
    }

    private fun listenForLatestNotifications(){
        adapter.clear()
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val user = p0.getValue(User::class.java)
                if(user != null && user.uid != thisUser){
                    adapter.add(UserItem(user))
                }
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d("Notification","Child removed")
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }
}