package com.example.chat_app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatAdapter(
    val currentUser : Int,
    val receiver: Int
): ListAdapter<Message, RecyclerView.ViewHolder>(diffUtil) {
    private val api = RetroInterface.create()

    override fun getItemViewType(position: Int): Int {
        return currentList[position].sender_id
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class YourViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val chatText = view.findViewById<TextView>(R.id.chatTextView)
        private val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        fun bind(message: Message){
            chatText.text = message.text
            dateTextView.text = message.time

            Log.d("testt", message.time)
        }
    }
    inner class OtherViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val chatText = view.findViewById<TextView>(R.id.chatTextView)
        private val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        private val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        fun bind(message: Message){
            chatText.text = message.text
            dateTextView.text = message.time
            api.getUserId(LoginResult(receiver)).enqueue(object: Callback<ArrayList<UserId>> {
                override fun onResponse(
                    call: Call<ArrayList<UserId>>,
                    response: Response<ArrayList<UserId>>
                ) {
                    val result = response.body()?:return
                    val id = result[0].id
                    nameTextView.text = id
                }

                override fun onFailure(call: Call<ArrayList<UserId>>, t: Throwable) {
                }
            })

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            currentUser -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_you, parent, false)
                YourViewHolder(view)
            }
            else ->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_other, parent, false)
                OtherViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(currentList[position].sender_id){
            currentUser->{
                (holder as YourViewHolder).bind(currentList[position])
                holder.setIsRecyclable(false)
            }
            else ->{
                (holder as OtherViewHolder).bind(currentList[position])
                holder.setIsRecyclable(false)
            }
        }
    }

    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<Message>(){
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    }
}