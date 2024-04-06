package com.example.umgmesage.messaging.Controllers

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.umgmesage.databinding.ItemForListChatBinding
import com.example.umgmesage.messaging.Models.Chat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatsViewHolder(view:View):RecyclerView.ViewHolder(view){

    private val binding = ItemForListChatBinding.bind(view)

    fun render(chat: Chat, onItemSelected: (Chat) -> Unit) {
        binding.tvChatName.text=chat.chatName
        binding.tvChatTimeStamp.text= dateFormatter(chat.lastMessageTimestamp.toDate())
        binding.tvChatLastMessage.text=chat.lastMessage
        if(chat.hasCustomIcon){
            TODO("Agregar codigo que obtenga imagen de cloudstorage y lo mande a traer")
        }else{
            binding.ivChatIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        binding.root.setOnClickListener { onItemSelected(chat) }

    }

    private fun dateFormatter(date: Date):String{
        val calendarReference=Calendar.getInstance()
        val thisDate=Calendar.getInstance()
        thisDate.time=date
        val formatSameDay = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatSameWeek = SimpleDateFormat("EEEE HH:mm", Locale.getDefault())
        val formatSameYear = SimpleDateFormat("d MMMM", Locale.getDefault())
        val formatDMY = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        Log.e(
            "ChatsViewHolder.dateFormatter", "${thisDate.get(Calendar.WEEK_OF_YEAR)} - ${calendarReference.get(Calendar.WEEK_OF_YEAR)}"
        )
        if (thisDate.get(Calendar.DAY_OF_YEAR) == calendarReference.get(Calendar.DAY_OF_YEAR) && thisDate.get(Calendar.YEAR) == calendarReference.get(Calendar.YEAR)) {
            Log.e(
                "ChatsViewHolder.dateFormatter", "Mismo día"
            )
            return formatSameDay.format(date)
        }else if (thisDate.get(Calendar.WEEK_OF_YEAR) == calendarReference.get(Calendar.WEEK_OF_YEAR) && thisDate.get(Calendar.YEAR) == calendarReference.get(Calendar.YEAR)) {
            Log.e(
                "ChatsViewHolder.dateFormatter", "Misma semana"
            )
            return formatSameWeek.format(date)
        }else if (thisDate.get(Calendar.YEAR) == calendarReference.get(Calendar.YEAR)) {
            Log.e(
                "ChatsViewHolder.dateFormatter", "Mismo año"
            )
            return formatSameYear.format(date)
        }else return formatDMY.format(date)
    }

}