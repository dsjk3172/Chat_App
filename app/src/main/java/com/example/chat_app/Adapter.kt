package com.example.chat_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chat_app.databinding.UserItemBinding

class Adapter(val itemClicked: (User)->(Unit)): ListAdapter<User, Adapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: UserItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(user: User){
            binding.idTextView.text = user.username

            binding.chatButton.setOnClickListener {
                itemClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<User>(){
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.UID == newItem.UID
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}