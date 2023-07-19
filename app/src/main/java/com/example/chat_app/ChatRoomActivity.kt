package com.example.chat_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.chat_app.databinding.ActivityChatRoomBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    val api = RetroInterface.create()
    private var sender = -1
    private var receiver = -1
    private lateinit var mSocket : Socket
    private var messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private var num = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 로그인한 유저의 uid 가져오기
        val sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        sender = sharedPreferences.getString("uid",null)!!.toInt()
        // 상대방 uid 가져오기
        receiver = intent.getIntExtra("receiver",-1)

        // 메세지 전송 버튼 비활성화 -> editText 에 내용이 있으면 활성화(밑에서 구현)
        binding.sendButton.isEnabled = false

        clickedEvent()
        getMessage() // 채팅 목록 가져오기

        adapter = ChatAdapter(sender,receiver)
        binding.chatRecyclerView.adapter = adapter
        try{
            // 소켓통신
            mSocket = IO.socket("http://ip:port")
            mSocket.connect()

        }catch (e: URISyntaxException){
            Log.d("testt",e.toString())
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect)

        // 채팅방 가져오기
        val room = Room(sender, receiver)
        api.getRoom(room).enqueue(object: Callback<ArrayList<RoomNumber>>{ // 현재 채팅방의 고유 번호를 가져옴
            override fun onResponse(call: Call<ArrayList<RoomNumber>>, response: Response<ArrayList<RoomNumber>>) {
                // ArrayList<RoomNumber> 형태로 서버로부터 응답이 옴
                val roomNumber = response.body() ?: return
                val number = roomNumber[0].number
                num = number // 채팅방 번호

                // 서버로부터 수신 대기 시켜놓음, 응답이 올경우 각각의 리스너를 실행(onNewUser, onNewMessage)
                mSocket.on("connect user", onNewUser) // connect user 라는 이름의 이벤트(서버로 부터 응답이 온 것)가 발생하면 onNewUser 리스너가 처리함
                mSocket.on("chat message", onNewMessage)

                val userId = JSONObject() // Json 객체 생성
                try {
                    userId.put("username", "$sender Connected")
                    userId.put("roomNumber", number)
                    Log.e("username", "$sender Connected")

                    // 서버로 전달
                    mSocket.emit("connect user", userId)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }

            override fun onFailure(call: Call<ArrayList<RoomNumber>>, t: Throwable) {

            }

        })




    }
    internal var onConnect = Emitter.Listener {
        mSocket.emit("connectReceive","OK")
    }
    internal var onNewUser = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val length = args.size

            if (length == 0) {
                return@Runnable
            }
            //Here i'm getting weird error..................///////run :1 and run: 0
            var username = args[0].toString()
            try {
                val `object` = JSONObject(username)
                username = `object`.getString("username")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        })
    }
    internal var onNewMessage = Emitter.Listener { args->
        // 새로운 메시지가 왔음!!
        // 리스트에 새로운 메시지를 넣고, notifyDataSetChanged!
        runOnUiThread(Runnable{
            val data = args[0] as JSONObject // 서버의 응답
            val sender_id:Int
            val receiver_id:Int
            val text:String
            val time:String

            try{
                Log.e("testt",data.toString())
                // value 를 꺼내서
                sender_id = data.getInt("sender_id")
                receiver_id = data.getInt("receiver_id")
                text = data.getString("text")
                time = data.getString("time")

                val messageTime = time
                val hms = messageTime.split(" ")[1]

                // 리스트에 전달할 객체로 저장하고
                val format = Message(sender_id, receiver_id, text, hms)

                // 리스트에 저장 후 리사이클러뷰 어답터에 전달
                messageList.add(format)
                adapter.submitList(messageList)
                adapter.notifyDataSetChanged()
                // 리사이클러뷰의 마지막 아이템으로 이동
                binding.chatRecyclerView.scrollToPosition(messageList.size-1)

                Log.e("new me", text)
            }catch (e: Exception){
                return@Runnable
            }
        })

    }
    private fun sendMessage(){
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val time = current.format(formatter)

        val text = binding.editText.text.toString()
        binding.editText.text.clear()
        val jsonObject = JSONObject() // json 객체를 생성해서 각각의 정보들을 넣어줌(보내는사람, 받는사람, 메세지, 시간, 채팅방 번호)
        try{
            jsonObject.put("sender_id",sender)
            jsonObject.put("receiver_id",receiver)
            jsonObject.put("text",text)
            jsonObject.put("time",time)
            jsonObject.put("roomNumber",num)
        }catch (e: JSONException){
            e.printStackTrace()
        }
        // 서버로 전달
        mSocket.emit("chat message", jsonObject)

        // 메세지를 db에 저장(insert)
        val message = Message(sender, receiver, text, time)
        api.sendMessage(message).enqueue(object: Callback<Message>{
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                response.body()?:return
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {}
        })

    }

    private fun clickedEvent(){
        // editText 가 비어있으면 전송 버튼 비활성화, 아니면 활성화
        binding.editText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.sendButton.isEnabled = !binding.editText.text.isBlank()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }
    private fun getMessage(){
        // 채팅 목록 가져오기(MySQL)
        val room = Room(sender, receiver)
        api.getMessage(room).enqueue(object: Callback<ArrayList<Message>>{
            override fun onResponse(
                call: Call<ArrayList<Message>>,
                response: Response<ArrayList<Message>>
            ) {
                val res = response.body()?: return
                messageList.clear()
                res.forEach {
                    messageList.add(it)
                }
                messageList.forEach {
                    val messageTime = it.time
                    val dateTime = messageTime.split("T")
                    val time = dateTime[1].split(".")[0]
                    it.time = time
                }

                adapter.submitList(messageList)
                adapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messageList.size-1)

            }

            override fun onFailure(call: Call<ArrayList<Message>>, t: Throwable) {
                Log.d("testt",t.message.toString())
            }
        })
    }

}
