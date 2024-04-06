package com.example.umgmesage.messaging.Controllers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.umgmesage.databinding.ItemForNewChatBinding
import com.example.umgmesage.messaging.Models.Chat
import com.example.umgmesage.messaging.Models.User

class NewChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemForNewChatBinding.bind(view)

    fun render(user: User, onItemSelected: (User) -> Unit) {
        binding.tvUserEmailNewChat.text=user.userEmail
        binding.tvUserNameNewChat.text=user.userName
        if(user.hasCustomIcon) {
            //binding.ivUserProfilePic=TODO("Agregar codigo que obtenga imagen de cloudstorage y lo mande a traer")
        }else{
            binding.ivUserProfilePic.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        binding.root.setOnClickListener{onItemSelected(user)}
    }
}