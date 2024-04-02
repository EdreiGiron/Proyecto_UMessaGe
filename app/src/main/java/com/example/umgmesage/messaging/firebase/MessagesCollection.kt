package com.example.umgmesage.messaging.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.umgmesage.messaging.Models.Message

class MessagesCollection(chatId: String) {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    val messagesCollectionReference =
        firestoreInstance.collection("Chats").document(chatId).collection("Messages")

    fun insertMessage(message: Message) {
        CoroutineScope(Dispatchers.IO).launch {
            messagesCollectionReference.add(message)
                .addOnSuccessListener { documentReference ->
                    Log.e(
                        "MessagesCollection.insertMessage",
                        "Message successfully inserted with ID ${documentReference.id}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("MessagesCollection.insertMessage", "Error inserting message: $e")
                }
        }
    }

    fun updateMessage(message: Message) {
        CoroutineScope(Dispatchers.IO).launch {
            messagesCollectionReference.document(message.messageId!!)
                .set(message)
                .addOnSuccessListener {
                    Log.e(
                        "MessagesCollection.updateMessage",
                        "Message successfully updated with ID ${message.messageId}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "MessagesCollection.insertMessage",
                        "Error updating the message with ID ${message.messageId}: $e"
                    )
                }
        }
    }

    fun deleteMessage(message: Message) {
        CoroutineScope(Dispatchers.IO).launch {
            messagesCollectionReference.document(message.messageId!!)
                .delete()
                .addOnSuccessListener {
                    Log.e(
                        "MessagesCollection.updateMessage",
                        "Message successfully deleted with ID ${message.messageId}"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "MessagesCollection.insertMessage",
                        "Error deleting the message with ID ${message.messageId}: $e"
                    )
                }
        }
    }

    fun messagesToList(onSuccess: (MutableList<Message>) -> Unit, onFailure: (Exception) -> Unit) {
        messagesCollectionReference.get()
            .addOnSuccessListener { messagesCollectionSnapshot ->
                val messagesList = mutableListOf<Message>()
                for (document in messagesCollectionSnapshot) {
                    val messagesCollectionDataMap = document.data
                    val messageRow = Message()
                    messagesCollectionDataMap.forEach { (key, value) ->
                        when (key) {
                            "messageTimestamp" -> messageRow.messageTimestamp =
                                document.getTimestamp("messageTimestamp") ?: Timestamp.now()
                            "senderId" -> messageRow.senderId
                            "text" -> messageRow.text = value.toString()
                            "hasAttachedImage" -> messageRow.hasAttachedImage =
                                document.getBoolean("hasAttachedImage")!!
                        }
                    }
                    messageRow.messageId = document.id
                    messagesList.add(messageRow)
                }
                onSuccess(messagesList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun documentToMessageItem(document:DocumentSnapshot): Message {
        val message= Message()
        document.data?.map {(key,value)->
            when(key){
                "text"->message.text=value.toString()
                "senderId"->message.senderId=value.toString()
                "messageTimestamp"->message.messageTimestamp=document.getTimestamp("messageTimestamp")?: Timestamp.now()
                "hasAttachedImage" -> message.hasAttachedImage =document.getBoolean("hasAttachedImage")?:false
            }
            message.messageId = document.id
        }
        return message
    }
}