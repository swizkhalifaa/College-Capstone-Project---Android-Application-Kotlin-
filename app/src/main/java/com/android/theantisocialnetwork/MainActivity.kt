package com.android.theantisocialnetwork

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.FragmentManager
import com.android.theantisocialnetwork.EntranceClasses.RegisterActivity
import com.android.theantisocialnetwork.Models.User
import com.android.theantisocialnetwork.Fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
        var currentMenuItem: MenuItem? = null
    }

    val uid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyUserIsLoggedIn()
        setContentView(R.layout.activity_main)

        fetchCurrentUser()
        setSupportActionBar(toolbarMain)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        listenForLatestNotifications()

        bottomNavigationView.setOnNavigationItemSelectedListener(lOnNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            val fragment = MessagesFragment()
            supportFragmentManager.beginTransaction().replace(R.id.containerOfFragments, fragment, fragment.javaClass.getSimpleName())
                .commit()
            textViewTitleMain.text = "Messages"
        }

        imageViewMainProfilePicture.setOnClickListener {
            logOut()
        }

        imageViewNotifications.setOnClickListener{
            val fragment = NotificationsFragment()
            supportFragmentManager.beginTransaction().replace(R.id.containerOfFragments, fragment, fragment.javaClass.getSimpleName())
                .addToBackStack(null)
                .commit()
            return@setOnClickListener
        }
    }




    // Navigation bar which loads main application fragments
    private val lOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        val shuffleLoadTime:Long = 300
        when (menuItem.itemId) {
            R.id.button_navigation_messages -> {
                // Ensure all fragments are popped from the backstack
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val fragment = MessagesFragment()
                supportFragmentManager.beginTransaction().replace(R.id.containerOfFragments, fragment, "Messages")
                    .commit()
                textViewTitleMain.text = "Messages"

                // Menu item is set and then current item variable is passed off to be reused
                currentMenuItem?.setEnabled(true)
                currentMenuItem = menuItem
                currentMenuItem?.setEnabled(false)

                return@OnNavigationItemSelectedListener true
            }

            R.id.button_navigation_create_post -> {
                // Ensure all fragments are popped from the backstack
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val fragment = CreateFragment()
                supportFragmentManager.beginTransaction().replace(R.id.containerOfFragments, fragment, fragment.javaClass.getSimpleName())
                    .addToBackStack(null)
                    .commit()
                textViewTitleMain.text = "Create Post"

                // Menu item is set and then current item variable is passed off to be reused
                currentMenuItem?.setEnabled(true)
                currentMenuItem = menuItem
                currentMenuItem?.setEnabled(false)

                return@OnNavigationItemSelectedListener true
            }

            R.id.button_navigation_shuffle_search -> {
                textViewTitleMain.text = "Discovery"
                Handler().postDelayed({
                    // Ensure all fragments are popped from the backstack
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    val fragment = ShuffleFragment()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.containerOfFragments,
                        fragment,
                        fragment.javaClass.getSimpleName()
                    )
                        .commit()

                    // Menu item is set and then current item variable is passed off to be reused
                    currentMenuItem?.setEnabled(true)
                    currentMenuItem = menuItem
                    currentMenuItem?.setEnabled(false)
                },shuffleLoadTime)
                return@OnNavigationItemSelectedListener true

            }

            R.id.button_navigation_hashtag_search -> {
                // Ensure all fragments are popped from the backstack
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val fragment = HashtagFragment()
                supportFragmentManager.beginTransaction().replace(R.id.containerOfFragments, fragment, fragment.javaClass.getSimpleName())
                    .commit()
                textViewTitleMain.text = "Search"

                // Menu item is set and then current item variable is passed off to be reused
                currentMenuItem?.setEnabled(true)
                currentMenuItem = menuItem
                currentMenuItem?.setEnabled(false)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }




    private fun fetchCurrentUser(){

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("Main", "Current user ${currentUser?.profileImageURL}")
                val uri = currentUser?.profileImageURL
                val targetImageView = imageViewMainProfilePicture
                Picasso.get().load(uri).into(targetImageView)
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun logOut(){
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, RegisterActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Log Out?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    private fun listenForLatestNotifications(){
        val notificationRef = FirebaseDatabase.getInstance().getReference("/user-notifications/$uid")

        notificationRef.addValueEventListener(object: ValueEventListener {
            // Changing notification logo when alerts are available
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.getValue() != null){
                    Log.d("Main",p0.childrenCount.toString())
                    imageViewNotifications.setBackgroundResource(R.drawable.ic_notification_bell_counter_24dp)
                    textViewNotificationCounter.text = p0.childrenCount.toString()
                }
                else{
                    textViewNotificationCounter.text = ""
                    imageViewNotifications.setBackgroundResource(R.drawable.ic_notification_bell_24dp)
                    Log.d("Main","No children")
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

}
