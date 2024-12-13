package com.example.testsehat.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testsehat.R
import com.example.testsehat.data.dataMusic

class MusicAdapter(private val onItemClick: (dataMusic) -> Unit) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    private var musicList: List<dataMusic> = listOf()

    fun updateMusic(newMusicList: List<dataMusic>) {
        musicList = newMusicList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return MusicViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.bind(music)
    }

    override fun getItemCount(): Int = musicList.size

    class MusicViewHolder(
        itemView: View,
        private val onItemClick: (dataMusic) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.musicTitleTextView)
        private val moodTextView: TextView = itemView.findViewById(R.id.musicMoodTextView)

        fun bind(music: dataMusic) {
            songNameTextView.text = music.song_name
            moodTextView.text = music.mood

            itemView.setOnClickListener {
                onItemClick(music)
            }
        }
    }
}