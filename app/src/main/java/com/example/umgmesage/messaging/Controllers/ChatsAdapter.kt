package com.example.umgmesage.messaging.Controllers

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.umgmesage.R
import com.example.umgmesage.messaging.Models.Chat

class ChatsAdapter(
    var chats: MutableList<Chat> = mutableListOf(), private val onItemSelected: (Chat) -> Unit
) :

    RecyclerView.Adapter<ChatsViewHolder>() {

    fun updateList(chats: MutableList<Chat>) {
        this.chats = chats.sortedByDescending { it.lastMessageTimestamp.toDate() }.toMutableList()
        Log.e(
            "ChatsAdapter.updateList", "Loading chats: $chats"
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_for_list_chat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.render(chats[position], onItemSelected)
    }

    override fun getItemCount() = chats.size

}