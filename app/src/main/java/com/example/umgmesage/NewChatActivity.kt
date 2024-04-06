package com.example.umgmesage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.umgmesage.databinding.ActivityNewChatBinding
import com.example.umgmesage.messaging.Controllers.NewChatAdapter
import com.example.umgmesage.messaging.Models.Chat
import com.example.umgmesage.messaging.Models.User
import com.example.umgmesage.messaging.firebase.ChatsCollection
import com.example.umgmesage.messaging.firebase.UsersCollection
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewChatBinding
    private lateinit var usersList: MutableList<User>
    private lateinit var chatsCollection: ChatsCollection
    private lateinit var usersCollection: UsersCollection
    private lateinit var userId: String
    private lateinit var newChatAdapter: NewChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        initUI()
        initListeners()
    }

    private fun initComponents() {
        userId = intent.getStringExtra("userId") ?: ""
        Log.e("NewChatActivity.initComponents", userId)
        chatsCollection = ChatsCollection(userId)
        usersCollection = UsersCollection()
        usersList = mutableListOf()
        subscribeToUserUpdates()

    }

    private fun subscribeToUserUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            usersCollection.userCollectionReference.whereNotEqualTo("userId", userId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        runOnUiThread {
                            Toast.makeText(
                                binding.root.context,
                                "Error en sincronización a Firestore: $it",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@addSnapshotListener
                    }

                    if (querySnapshot?.documentChanges != null) {
                        for (dc in querySnapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    usersList.add(usersCollection.documentToUserItem(dc.document))
                                }

                                DocumentChange.Type.REMOVED -> {
                                    usersList.removeAt(usersList.indexOfFirst {
                                        it.userId == dc.document.getString(
                                            "userId"
                                        )
                                    })
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val userToUptade =
                                        usersList.find { it.userId == dc.document.id }!!
                                    userToUptade.apply {
                                        this.userName = dc.document.getString("userName").orEmpty()
                                        this.userEmail =
                                            dc.document.getString("userEmail").orEmpty()
                                        this.hasCustomIcon =
                                            dc.document.getBoolean("hasCustomIcon") ?: false
                                        this.userId = dc.document.getString("userId").orEmpty()
                                    }
                                }
                            }

                        }


                        runOnUiThread { newChatAdapter.updateList(usersList) }
                    }
                }
        }
    }


    private fun initListeners() {
        binding.btnStartChat.setOnClickListener {
            val newChat = Chat()
            newChat.chatName = binding.newChatNameInput.text.toString()
            newChat.creatorId = userId
            newChat.administratorsId = listOf(userId)
            newChat.lastMessage = "Se ha creado el chat."
            newChat.lastMessageTimestamp = Timestamp.now()
            newChat.hasCustomIcon =
                false //TODO("Agregar funcion que valide carga de imagen y lo cambie a true")
            newChat.creationTimestamp = Timestamp.now()
            val membersList: MutableList<String> = mutableListOf()
            membersList.add(userId)
            binding.newChatReciclerView.forEach {
                val cbItem = it.findViewById<CheckBox>(R.id.cbAddNewChat)
                if (cbItem.isChecked) {
                    membersList.add(usersList.find { user ->
                        user.userEmail == it.findViewById<TextView>(
                            R.id.tvUserEmailNewChat
                        ).text.toString()
                    }!!.userId.toString())
                }
            }
            newChat.membersId = membersList.toList()
            createNewChatTask(newChat)
        }

        binding.backBtnNewChat.setOnClickListener {
            super.finish()
        }
    }

   fun createNewChatTask(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            val chatId: String? = chatsCollection.insertChat(chat)
            super.finish()
        }
    }


    private fun initUI() {
        newChatAdapter = NewChatAdapter(usersList) {user -> setUserSelected(user)}
        binding.newChatReciclerView.layoutManager = LinearLayoutManager(this)
        binding.newChatReciclerView.adapter = newChatAdapter
    }

    private fun setUserSelected(view:ViewGroup) {
        view.findViewById<CheckBox>(R.id.cbAddNewChat).toggle()
    }

    private fun navigateToNewChatRoom(chatId: String) {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("chatId", chatId)
        Log.e("navigateToChatRoom", "$userId - ${chatId}")
        startActivity(intent)
    }
}