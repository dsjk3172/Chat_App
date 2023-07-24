package com.example.chat_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chat_app.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val api = RetroInterface.create()
    lateinit var allUser : ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
            val intent = Intent(this@MainActivity, UserRegister::class.java)
            startActivity(intent)
        }
        binding.loginButton.setOnClickListener {
            binding.apply {
                val id = inputID.text.toString()
                val pw = inputPw.text.toString()

                if(id == "" || pw == "") {
                    Toast.makeText(applicationContext, "입력하지 않은 정보가 있습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val loginUser = LoginModel(binding.inputID.text.toString(), binding.inputPw.text.toString())
            api.login(loginUser).enqueue(object: Callback<LoginResult>{
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    val user_uid = response.body()?.UID ?: return
                    if(user_uid != -1) {
                        Toast.makeText(applicationContext, "로그인 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, AllUserActivity::class.java)


                        intent.putExtra("userList", allUser)
                        startActivity(intent)

                        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("uid", user_uid.toString())
                        editor.apply()

                        Log.d("testt", user_uid.toString())
                    }
                    else{
                        Toast.makeText(applicationContext, "로그인 실패, 아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    Log.d("testt", t.message.toString())
                }
            })
        }

    }
}