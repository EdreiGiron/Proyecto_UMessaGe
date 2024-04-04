package com.example.umgmesage

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.umgmesage.databinding.ActivityChatRoomBinding
import com.example.umgmesage.messaging.Controllers.MessageAdapter
import com.example.umgmesage.messaging.Models.Chat
import com.example.umgmesage.messaging.Models.Message
import com.example.umgmesage.messaging.Models.User
import com.example.umgmesage.messaging.firebase.ChatsCollection
import com.example.umgmesage.messaging.firebase.MessagesCollection
import com.example.umgmesage.messaging.firebase.UsersCollection
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatCollection: ChatsCollection
    private lateinit var messagesCollection: MessagesCollection
    private lateinit var messageList: MutableList<Message>
    private lateinit var usersCollection: UsersCollection
    private lateinit var userList: MutableList<User>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chat: Chat
    private lateinit var userId: String
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        subscribeToUsersUpdates()
        subscribeToMessageUpdates()
        subscribeToChatUpdates()
        initUI()
        initListeners()
    }

    private fun initComponents() {
        userId = intent.getStringExtra("userId").orEmpty()
        chatId = intent.getStringExtra("chatId").orEmpty()
        chatCollection = ChatsCollection(userId)
        usersCollection = UsersCollection()
        messagesCollection = MessagesCollection(chatId)
        userList = mutableListOf()
        messageList = mutableListOf()
    }

    private fun initUI() {
        messageAdapter = MessageAdapter(messageList) {}
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

    }

    private fun initListeners() {
        binding.messageSendBtn.setOnClickListener {
            val newMessage = Message()
            newMessage.senderId = userId
            newMessage.text = binding.chatMessageInput.text.toString()
            newMessage.messageTimestamp = Timestamp.now()
            messagesCollection.insertMessage(newMessage)
            chat.lastMessage="${userList.find { it.userId==userId }!!.userName}: ${newMessage.text}"
            chat.lastMessageTimestamp=newMessage.messageTimestamp
            chatCollection.updateChat(chat)
        }

        binding.backBtn.setOnClickListener {
            super.finish()
        }
    }

    private fun subscribeToChatUpdates() {
        CoroutineScope(Dispatchers.IO).launch() {
            chatCollection.userChatsList.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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
                        chat = when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                chatCollection.documentToChatItem(dc.document)
                            }

                            DocumentChange.Type.REMOVED -> {
                                chatCollection.documentToChatItem(dc.document)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                chatCollection.documentToChatItem(dc.document)
                            }
                        }

                        }
                    runOnUiThread{binding.otherUsername.text = chat.chatName}
                }
            }
        }
    }

    private fun subscribeToMessageUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            messagesCollection.messagesCollectionReference.orderBy(
                "messageTimestamp", Query.Direction.DESCENDING
            ).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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
                                messageList.add(messagesCollection.documentToMessageItem(dc.document))
                            }

                            DocumentChange.Type.REMOVED -> messageList.removeAt(messageList.indexOfFirst { it.messageId == dc.document.id })
                            DocumentChange.Type.MODIFIED -> {
                                val chatToUpdate =
                                    messageList.find { it.messageId == dc.document.id }
                                chatToUpdate?.apply {
                                    this.text = dc.document.getString("text").orEmpty()
                                    this.messageTimestamp =
                                        dc.document.getTimestamp("messageTimestamp")
                                            ?: Timestamp.now()
                                    this.senderId = dc.document.getString("senderID").orEmpty()
                                    this.hasAttachedImage =
                                        dc.document.getBoolean("hasAttachedImage") ?: false
                                }
                            }

                        }

                    }
                    runOnUiThread {
                        messageAdapter.updateList(messageList, userList)
                    }
                }
            }
        }
    }

    private fun subscribeToUsersUpdates() {
        CoroutineScope(Dispatchers.IO).launch {
            usersCollection.userCollectionReference.orderBy("userName")
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
                                    userList.add(usersCollection.documentToUserItem(dc.document))
                                }

                                DocumentChange.Type.REMOVED -> userList.removeAt(userList.indexOfFirst {
                                    it.userId == dc.document.data.get(
                                        "userId"
                                    ).toString()
                                })

                                DocumentChange.Type.MODIFIED -> {
                                    val userToUpdate = userList.find {
                                        it.userId == dc.document.data.get("userId").toString()
                                    }
                                    userToUpdate?.apply {
                                        this.userName = dc.document.getString("userName").orEmpty()
                                        this.userEmail =
                                            dc.document.getString("userEmail").orEmpty()
                                        this.hasCustomIcon =
                                            dc.document.getBoolean("hasCustomIcon") ?: false
                                    }
                                }
                            }
                        }
                        runOnUiThread {
                            messageAdapter.updateList(messageList, userList)
                        }
                    }
                }
        }
    }
}
