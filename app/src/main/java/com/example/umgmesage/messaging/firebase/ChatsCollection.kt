package com.example.umgmesage.messaging.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.umgmesage.messaging.Models.Chat

class ChatsCollection(userId: String) {
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val chatCollectionReference = firestoreInstance.collection("Chats")
    val userChatsList: Query = chatCollectionReference.whereArrayContains("membersId", userId)
        .orderBy("creationTimestamp", Query.Direction.DESCENDING)

    fun insertChat(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            chat.creationTimestamp = Timestamp.now()
            chatCollectionReference.add(chat).addOnSuccessListener { documentReference ->
                Log.e(
                    "ChatsCollection.insertChat",
                    "Chat successfully inserted with ID ${documentReference.id}"
                )
            }.addOnFailureListener { e ->
                Log.e("ChatsCollection.insertChat", "Error inserting chat: $e")
            }
        }
    }

    fun updateChat(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            val chatId = chat.chatId.orEmpty()
            chat.chatId = null
            chatCollectionReference.document(chatId).set(chat).addOnSuccessListener {
                Log.e(
                    "ChatsCollection.updateChat", "Chat successfully updated with ID $chatId"
                )
            }.addOnFailureListener { e ->
                Log.e(
                    "ChatsCollection.updateChat", "Error updating the chat with ID $chatId: $e"
                )
            }
        }
    }

    fun deleteChat(chat: Chat) {
        CoroutineScope(Dispatchers.IO).launch {
            chatCollectionReference.document(chat.chatId.orEmpty()).delete().addOnSuccessListener {
                Log.e(
                    "ChatsCollection.deleteChat", "Chat successfully deleted with ID ${chat.chatId}"
                )
            }.addOnFailureListener { e ->
                Log.e(
                    "ChatsCollection.deleteChat",
                    "Error deleting the chat with ID ${chat.chatId}: $e"
                )
            }
        }
    }

    fun chatsToList(
        onSuccess: (MutableList<Chat>) -> Unit, onFailure: (MutableList<Chat>) -> Unit
    ) {
        val chatsList = mutableListOf<Chat>()
        userChatsList.get().addOnSuccessListener { userChatsCollectionSnapshot ->
            for (document in userChatsCollectionSnapshot) {
                val chatRow= documentToChatItem(document)
                chatsList.add(chatRow)
            }
            Log.e(
                "ChatsCollection.chatsToList",
                "Consult to Chats Collection successfull with ${chatsList.size} registers."
            )
            onSuccess(chatsList)
        }.addOnFailureListener { e ->
            Log.e(
                "ChatsCollection.chatsToList", "Error on consult to Chats Collection : $e"
            )
            onFailure(chatsList)
        }
    }

    fun getChat(chatId: String): Chat {
        var chat = Chat()
        chatCollectionReference.document(chatId).get().addOnSuccessListener {
            chat = documentToChatItem(it)
        }
        return chat
    }

    private fun documentToChatItem(document: DocumentSnapshot): Chat {
        val chatRow = Chat()
        document.data?.forEach { (key, value) ->
            when (key) {
                "chatName" -> {
                    chatRow.chatName = value.toString()
                }

                "creationTimestamp" -> {
                    chatRow.creationTimestamp = document.getTimestamp("creationTimestamp")?:Timestamp.now()
                }

                "hasCustomIcon" -> {
                    chatRow.hasCustomIcon = document.getBoolean("hasCustomIcon") ?: false
                }

                "creatorId" -> {
                    chatRow.creatorId = value.toString()
                }

                "membersId" -> {
                    chatRow.membersId =
                        (document.get("membersId") as ArrayList<String>).toTypedArray()
                }

                "administratorsId" -> {
                    chatRow.administratorsId =
                        (document.get("administratorsId") as ArrayList<String>).toTypedArray()
                }

                "lastMessage" -> {
                    chatRow.lastMessage = value.toString()
                }
            }
            chatRow.chatId = document.id
        }
        return chatRow

    }

    fun documentToChatItem(document: QueryDocumentSnapshot): Chat {
        val chatRow = Chat()
        val dataMap = document.data
        dataMap.forEach { (key, value) ->
            when (key) {
                "chatName" -> chatRow.chatName = value.toString()
                "creationTimestamp" -> chatRow.creationTimestamp =
                    document.getTimestamp("creationTimestamp")!!

                "hasCustomIcon" -> chatRow.hasCustomIcon =
                    document.getBoolean("hasCustomIcon") ?: false

                "creatorId" -> chatRow.creatorId = value.toString()
                "membersId" -> {
                    chatRow.membersId =
                        (document.get("membersId") as ArrayList<String>).toTypedArray()
                }

                "administratorsId" -> {
                    chatRow.administratorsId =
                        (document.get("administratorsId") as ArrayList<String>).toTypedArray()
                }

                "lastMessage" -> chatRow.lastMessage = value.toString()
            }
            chatRow.chatId = document.id
        }
        return chatRow
    }
}