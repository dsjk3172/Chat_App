package com.example.chat_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chat_app.databinding.ActivityUserRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRegister : AppCompatActivity() {
    private lateinit var binding: ActivityUserRegisterBinding
    val api = RetroInterface.create()
    lateinit var allUser : ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api.allUser().enqueue(object: retrofit2.Callback<ArrayList<User>>{
            override fun onResponse(
                call: Call<ArrayList<User>>,
                response: Response<ArrayList<User>>
            ) {
                allUser = response.body()?:return
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {

            }
        })
        binding.registerButton.setOnClickListener {
            binding.apply {
                val id = inputID.text.toString()
                val pw = inputPw.text.toString()
                val username = inputUsername.text.toString()

                if(id == "" || pw == ""|| username == "") {
                    Toast.makeText(applicationContext, "입력하지 않은 정보가 있습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            val newUser = RegisterModel(binding.inputID.text.toString(), binding.inputPw.text.toString(), binding.inputUsername.text.toString())
            api.register(newUser).enqueue(object: retrofit2.Callback<RegisterResult>{
                override fun onResponse(call: Call<RegisterResult>, response: Response<RegisterResult>) {
                    val result = response.body()?.message ?: return
                    if(result){
                        Toast.makeText(applicationContext, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@UserRegister, MainActivity::class.java)
                        startActivity(intent)}
                    else
                        Toast.makeText(applicationContext, "회원가입 실패, 이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<RegisterResult>, t: Throwable) {
                    Log.d("testt", t.message.toString())
                }
            })
        }

    }
}