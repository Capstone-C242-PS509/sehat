package com.example.testsehat.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testsehat.R
import dataChat


class ChatAdapter(private val messages: List<dataChat>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContainer: View = view.findViewById(R.id.messageContainer)
        val messageText: TextView = view.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageText.text = message.text

        val layoutParams = holder.messageContainer.layoutParams as ViewGroup.MarginLayoutParams
        if (message.isSender) {
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 50
            holder.messageContainer.layoutParams = layoutParams
            (holder.messageContainer.parent as LinearLayout).gravity = Gravity.END
            holder.messageContainer.setBackgroundResource(R.drawable.sender_background)
        } else {
            layoutParams.marginStart = 50
            layoutParams.marginEnd = 0
            holder.messageContainer.layoutParams = layoutParams
            (holder.messageContainer.parent as LinearLayout).gravity = Gravity.START
            holder.messageContainer.setBackgroundResource(R.drawable.receiver_background)
        }
    }

    override fun getItemCount(): Int = messages.size
}

