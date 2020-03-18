package dev.forcetower.networking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.forcetower.networking.databinding.ActivityMainBinding
import java.util.concurrent.Executors

// This code is an example, don't put all your code in a activity or a single class...
// this is a brain dead created app just for showing firebase stuff
// no effort what so ever was made to make this a clean android code.

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: MessagesAdapter
    private val executors = Executors.newFixedThreadPool(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        adapter = MessagesAdapter()
        binding.recycler.adapter = adapter
        initMessages()

        binding.btnLogin.setOnClickListener { signIn() }
        binding.send.setOnClickListener { send() }
    }

    private fun initMessages() {
        firestore.collection("messages").orderBy("createdAt").addSnapshotListener { snapshot, _ ->
            snapshot ?: return@addSnapshotListener
            val messages = snapshot.documents.mapNotNull {
                val id = it.id
                val content = it["content"] as String
                val sender = it["sender"] as String
                Message(id, content, sender)
            }
            adapter.submitList(messages)
            Handler().postDelayed({
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    binding.recycler.scrollToPosition(messages.size - 1)
                }
            }, 200)
        }
    }

    private fun send() {
        Log.d("AAAA", "AAAAAAAAAAAA")
        val text = binding.input.text.toString()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            executors.execute {
                Tasks.await(
                    firestore.collection("messages").add(
                        mapOf(
                            "userId" to user.uid,
                            "content" to text
                        )
                    )
                )
            }
        } else {
            AlertDialog.Builder(this)
                .setTitle("Disconnected")
                .setMessage("Please, login before trying")
                .create()
                .show()
        }
    }

    private fun signIn() {
        val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            val user = FirebaseAuth.getInstance().currentUser

            if (resultCode == Activity.RESULT_OK && user != null) {

                firestore.collection("users").document(user.uid).set(
                    mapOf(
                        "displayName" to user.displayName,
                        "email" to user.email,
                        "photoURL" to user.photoUrl.toString()
                    )
                )

                AlertDialog.Builder(this)
                    .setTitle("Connected")
                    .setMessage("Welcome ${user.displayName}")
                    .create()
                    .show()
            } else {
                val code = response?.error?.errorCode
                AlertDialog.Builder(this)
                    .setTitle("Login error")
                    .setMessage("Can't login! Error $code")
                    .create()
                    .show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 5000
    }
}
