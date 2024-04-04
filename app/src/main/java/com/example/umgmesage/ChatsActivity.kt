package com.example.umgmesage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.umgmesage.databinding.ActivityChatsBinding
import com.example.umgmesage.messaging.Models.Chat
import com.example.umgmesage.messaging.Controllers.ChatsAdapter
import com.example.umgmesage.messaging.firebase.ChatsCollection

class ChatsActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var binding: ActivityChatsBinding
    private lateinit var userChatsCollection: ChatsCollection
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var chatsList: MutableList<Chat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        initUI()
    }

    private fun initComponents() {
        userId=intent.getStringExtra("userId") ?:""
        Log.e("ChatsActivity.initComponents",userId)
        userChatsCollection = ChatsCollection(userId)
        chatsList = mutableListOf()
        subscribeToChatUpdates()
    }

    private fun initUI() {
        chatsAdapter = ChatsAdapter(chatsList) { chat -> navigateToChatRoom(chat)}
        binding.rvChatList.layoutManager = LinearLayoutManager(this)
        binding.rvChatList.adapter = chatsAdapter
    }

    private fun navigateToChatRoom(chat: Chat) {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("chatId",chat.chatId)
        Log.e("navigateToChatRoom","$userId - ${chat.chatId}")
        startActivity(intent)
    }

    private fun subscribeToChatUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            userChatsCollection.userChatsList.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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
                if(querySnapshot?.documentChanges!=null) {
                for (dc in querySnapshot.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            chatsList.add(
                                userChatsCollection.documentToChatItem(
                                    dc.document
                                )
                            )
                        }
                        DocumentChange.Type.REMOVED -> chatsList.removeAt(chatsList.indexOfFirst { it.chatId == dc.document.id })
                        DocumentChange.Type.MODIFIED -> {
                            val chatToUpdate = chatsList.find { it.chatId == dc.document.id }!!
                            chatToUpdate.apply {
                                this.chatName = dc.document.getString("chatName").orEmpty()
                                this.lastMessage = dc.document.getString("lastMessage").orEmpty()
                                this.membersId =
                                    (dc.document.get("membersId") as ArrayList<String>).toTypedArray()
                                this.administratorsId =
                                    (dc.document.get("administratorsId") as ArrayList<String>).toTypedArray()
                                this.lastMessageTimestamp =
                                    dc.document.getTimestamp("lastMessageTimestamp")
                                        ?: Timestamp.now()
                                this.creationTimestamp =
                                    dc.document.getTimestamp("creationTimestamp")
                                        ?: Timestamp.now()
                                this.hasCustomIcon =
                                    dc.document.getBoolean("hasCustomIcon") ?: false
                            }
                        }

                    }

                }
                runOnUiThread {
                    chatsAdapter.updateList(chatsList)
                }
                }
            }
        }
    }

    private fun initListeners(){
        binding.cvAddChat.setOnClickListener {
            val intent = Intent(this, ChatRoomActivity::class.java)
            startActivity(intent)
        }
    }

}