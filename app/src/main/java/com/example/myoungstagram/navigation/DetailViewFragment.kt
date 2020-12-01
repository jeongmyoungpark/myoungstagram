package com.example.myoungstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myoungstagram.R
import com.example.myoungstagram.navigation.model.AlarmDTO
import com.example.myoungstagram.navigation.model.ContentDTO
import com.example.myoungstagram.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment :Fragment(){
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter= DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    //모든 func상속 받기.
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf() //리스트 초기화
        var contentUidList : ArrayList<String> = arrayListOf() //uid담을 리스트

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener {querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                if (querySnapshot == null) return@addSnapshotListener //signout 시에 오류를 막기위함.

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view= LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![p1].userId

            //image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //content 설명
            viewholder.detailviewitem_explain_textview.text= contentDTOs!![p1].explain

            //likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes "+contentDTOs!![p1].favoriteCount

            //event in Likes button
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1) //p1=position 값을 넘겨준다.
            }

            //좋아요 이벤트시 하트색칠 혹은 비우는 것
            if(contentDTOs!![p1].favorites.containsKey(uid)){
                //좋아요 상태
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite) //꽉찬 하트
            }
            else{
                //싫어요 상태
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border) //빈 하트
            }
            //프로필 이미지 클릭 됬을때
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[p1].uid)
                bundle.putString("userId",contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[p1])
                intent.putExtra("destinationUid",contentDTOs[p1].uid)
                startActivity(intent)
            }
        }
        fun favoriteEvent(position : Int) {
            //선택한 파일의 uid 를 찾아
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO= transaction.get(tsDoc!!).toObject(ContentDTO::class.java)//트랜젝션을 캐스팅

                //좋아요버튼 눌렸을때와 안눌렸을때
                if (contentDTO!!.favorites.containsKey(uid)){
                    //버튼 클릭 되있을때 취소하는 과정
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else{
                    //안눌려있을때 +1 하는과정
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }
                transaction.set(tsDoc,contentDTO) //트랜잭션을 적용시키기 위해 서버로 돌려준다
            }
        }
        fun  favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid,"myoungstagram",message)
        }

    }
}