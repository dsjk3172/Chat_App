package com.example.chat_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chat_app.databinding.ActivityAllUserBinding
import retrofit2.Call
import retrofit2.Response

class AllUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllUserBinding
    val api = RetroInterface.create()
    val userList = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val currentUid = sharedPreferences.getString("uid",null)

        val intent = intent
        val users = intent.getSerializableExtra("userList") as ArrayList<User>


        users.forEach {
            if(it.UID != currentUid?.toInt()) // 현재 로그인한 유저는 리스트에서 제외
                userList.add(it)
        }

        val adapter = Adapter { user ->
            // 대화방 만들고 대화창으로 이동
            if(currentUid == null) return@Adapter

            val room = Room(currentUid.toInt(), user.UID)
            api.createRoom(room).enqueue(object: retrofit2.Callback<RegisterResult>{
                override fun onResponse(
                    call: Call<RegisterResult>,
                    response: Response<RegisterResult>
                ) {
                    val result = response.body()?.message ?: return
                    if(result){
                        Toast.makeText(this@AllUserActivity, "새로운 대화방이 생성되었습니다",Toast.LENGTH_SHORT).show()
                        goChat(user)
                    }else{
                        Toast.makeText(this@AllUserActivity, "이어서 대화를 합니다",Toast.LENGTH_SHORT).show()
                        goChat(user)
                    }
                }

                override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                    Log.d("testt",t.message.toString())
                }
            })

        }
        binding.recyclerView.adapter = adapter
        adapter.submitList(userList)

    }
    private fun goChat(user: User){
        val chatIntent = Intent(this@AllUserActivity, ChatRoomActivity::class.java)
        chatIntent.putExtra("receiver", user.UID)
        startActivity(chatIntent)
    }
}