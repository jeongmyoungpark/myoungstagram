package com.example.myoungstagram.navigation.util

import com.example.myoungstagram.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey ="AAAA64V1C3s:APA91bFHXKGZFK3eQ4grf8nMRnGQEB7aiUGhoelRUeTdXNPQskG4hrEkhXwriuC2oVWjfuZsurrxQMQ2LEyUIz1ZNI3EXi6ykgErJ4YlIEvkIRqsfZUweozqfH0Ula1kiIR5gvzeGepC"
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    companion object{
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushDTO =PushDTO()
                pushDTO.to =token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
                var request = Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization","key="+serverKey)
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback{
                    override fun onFailure(call: Call?, e: IOException?) {
                        TODO("Not yet implemented")//실패했을때
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        //성공할때
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }
}