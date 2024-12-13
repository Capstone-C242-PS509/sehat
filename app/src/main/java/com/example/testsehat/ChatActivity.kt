package com.example.testsehat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testsehat.fragment.ChatFragment


class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatFragment())
                .commit()
        }
    }
}

