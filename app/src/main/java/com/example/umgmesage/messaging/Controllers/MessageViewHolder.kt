package com.example.umgmesage.messaging.Controllers

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.umgmesage.databinding.ItemForListChatRoomBinding
import com.example.umgmesage.messaging.Models.Message
import com.example.umgmesage.messaging.Models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageViewHolder(view: View): RecyclerView.ViewHolder(view){

    private val binding = ItemForListChatRoomBinding.bind(view)

    fun render(message: Message, user: User, onItemSelected: (Message) -> Unit) {
        binding.tvUserName.text=user.userName
        binding.tvMessage.text= message.text
        binding.tvMessageTimeStamp.text=dateFormatter(message.messageTimestamp.toDate())
        if(message.hasAttachedImage){
            TODO("Agregar codigo que obtenga imagen de cloudstorage y lo mande a traer")
        }else{
            binding.ivUserIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        binding.root.setOnClickListener { onItemSelected(message) }
    }

    private fun dateFormatter(date: Date):String{
        val calendarReference= Calendar.getInstance()
        val thisDate= Calendar.getInstance()
        thisDate.time=date
        val formatSameDay = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatSameWeek = SimpleDateFormat("EEEE", Locale.getDefault())
        val formatSameYear = SimpleDateFormat("d MMMM", Locale.getDefault())
        val formatDMY = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        Log.e(
            "ChatsViewHolder.dateFormatter", "${thisDate.get(Calendar.WEEK_OF_YEAR)} - ${calendarReference.get(
                Calendar.WEEK_OF_YEAR)}"
        )
        if (thisDate.get(Calendar.DAY_OF_YEAR) == calendarReference.get(Calendar.DAY_OF_YEAR) && thisDate.get(
                Calendar.YEAR) == calendarReference.get(Calendar.YEAR)) {
            Log.e(
                "ChatsViewHolder.dateFormatter", "Mismo día"
            )
            return formatSameDay.format(date)
        }else if (thisDate.get(Calendar.WEEK_OF_YEAR) == calendarReference.get(Calendar.WEEK_OF_YEAR) && thisDate.get(
                Calendar.YEAR) == calendarReference.get(Calendar.YEAR)) {
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