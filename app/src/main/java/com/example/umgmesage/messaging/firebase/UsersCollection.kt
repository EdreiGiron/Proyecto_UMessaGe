package com.example.umgmesage.messaging.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.umgmesage.messaging.Models.User

class UsersCollection {
    private val firestoreInstance = FirebaseFirestore.getInstance()
    val userCollectionReference = firestoreInstance.collection("Users")
    private val userList: Query = userCollectionReference.orderBy("userName", Query.Direction.DESCENDING)

    fun insertUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userCollectionReference.add(user).addOnSuccessListener { documentReference ->
                Log.e(
                    "UserCollection.insertUser",
                    "User successfully inserted with ID ${documentReference.id}"
                )
            }.addOnFailureListener { e ->
                Log.e("UsersCollection.insertUser", "Error inserting user: $e")
            }
        }
    }

    fun updateUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = user.userId.orEmpty()
            user.userId = null
            userCollectionReference.document(userId).set(user).addOnSuccessListener {
                Log.e(
                    "UsersCollection.updateUser", "User successfully updated with ID $userId"
                )
            }.addOnFailureListener { e ->
                Log.e(
                    "UsersCollection.updateUser", "Error updating the user with ID $userId: $e"
                )
            }
        }
    }

    fun deleteUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userCollectionReference.document(user.userId.orEmpty()).delete().addOnSuccessListener {
                Log.e(
                    "UsersCollection.deleteUser", "User successfully deleted with ID ${user.userId}"
                )
            }.addOnFailureListener { e ->
                Log.e(
                    "UsersCollection.deleteUser",
                    "Error deleting the user with ID ${user.userId}: $e"
                )
            }
        }
    }

    fun usersToList(
        onSuccess: (MutableList<User>) -> Unit, onFailure: (MutableList<User>) -> Unit
    ) {
        val chatsList = mutableListOf<User>()
        userList.get().addOnSuccessListener { userChatsCollectionSnapshot ->
            for (document in userChatsCollectionSnapshot) {
                val user: User = documentToUserItem(document)
                chatsList.add(user)
            }
            Log.e(
                "UsersCollection.usersToList",
                "Consult to Users Collection successfully with ${chatsList.size} registers."
            )
            onSuccess(chatsList)
        }.addOnFailureListener { e ->
            Log.e(
                "UsersCollection.usersToList", "Error on consult to Users Collection : $e"
            )
            onFailure(chatsList)
        }
    }

    fun getUser(userId: String): User {
        var user = User()
        userCollectionReference.whereEqualTo("userId",userId).get().addOnSuccessListener {
                documentToUserItem(it.first())
            }
        return user
    }

    fun documentToUserItem(document: DocumentSnapshot): User {
        val userRow = User()
        if(document.data!=null){
        userRow.userName = document.data!!["userName"] as String
        userRow.userId = document.data!!["userId"] as String
        userRow.userEmail = document.data!!["userEmail"] as String
        userRow.hasCustomIcon=document.data!!["hasCustomIcon"] as Boolean
        }
        Log.e("documentToUserItem","useriD=$userRow")
        return userRow
    }
    }