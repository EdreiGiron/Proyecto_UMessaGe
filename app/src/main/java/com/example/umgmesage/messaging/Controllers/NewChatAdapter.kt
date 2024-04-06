package com.example.umgmesage.messaging.Controllers

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.umgmesage.R
import com.example.umgmesage.messaging.Models.Chat
import com.example.umgmesage.messaging.Models.User

class NewChatAdapter(
    var users: MutableList<User> = mutableListOf(), private val onItemSelected: (User) -> Unit
) :

    RecyclerView.Adapter<NewChatViewHolder>() {

    fun updateList(users: MutableList<User>) {
        this.users = users.sortedBy {it.userName }.toMutableList()
        Log.e(
            "NewChatAdapter.updateList", "Loading users: $users"
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatViewHolder {
        return NewChatViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_for_new_chat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewChatViewHolder, position: Int) {
        holder.render(users[position], onItemSelected)
    }

    override fun getItemCount() = users.size

}