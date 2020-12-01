package com.example.myoungstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myoungstagram.R
import com.example.myoungstagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0 //request code
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null //imageurl 담을곳
    var auth : FirebaseAuth? =null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //strage 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //앨범 열리기
        var photoPickerIntent =Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //버튼 이벤트
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK) {
                //사진 선택시 이미지경로
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri) //addphoto_image view에 이미지

            }else{
                //취소 버튼 클릭시 작동경로
                finish()
            }
        }
    }
    fun contentUpload(){
        //파일 이름 생성
        var timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())//중복을 피하기위해 시간값으로
        var imageFileName = "IMAGE_"+timestamp+"_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask{ task: Task<UploadTask.TaskSnapshot> ->
         return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //이미지의 url 삽입
            contentDTO.imageUrl = uri.toString()

            //사용자의 uid삽입
            contentDTO.uid= auth?.currentUser?.uid

            //유저id 삽입
            contentDTO.userId= auth?.currentUser?.email

            //설명 삽입
            contentDTO.explain = addphoto_edit_explain.text.toString()

            //시간 삽입
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)//dto를 컬렉션 images에 넣어줌

            setResult(Activity.RESULT_OK)  // 정상적으로 닫혔음을 알리기위해

            finish()
        }
/*
        //callback method 파일 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //이미지 업로드 완료시 이미지 주소 받아옴.
            storageRef.downloadUrl.addOnSuccessListener { uri->
                var contentDTO = ContentDTO()

                //이미지의 url 삽입
                contentDTO.imageUrl = uri.toString()

                //사용자의 uid삽입
                contentDTO.uid= auth?.currentUser?.uid

                //유저id 삽입
                contentDTO.userId= auth?.currentUser?.email

                //설명 삽입
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //시간 삽입
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)//dto를 컬렉션 images에 넣어줌

                setResult(Activity.RESULT_OK)  // 정상적으로 닫혔음을 알리기위해

                finish()
            }
        }*/
    }
}