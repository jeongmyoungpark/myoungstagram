package com.example.myoungstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myoungstagram.R
import com.example.myoungstagram.navigation.UserFragment.UserFragmentRecyclerViewAdapter
import com.example.myoungstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.*

class GridFragment :Fragment(){
    var firestore : FirebaseFirestore?= null
    var fragmentView : View?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)
        firestore = FirebaseFirestore.getInstance()
        fragmentView?.gridfragment_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity, 3)
        return fragmentView
    }
        inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
            var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
            init {
                firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //쿼리가 비었을때 반환
                    if (querySnapshot==null) return@addSnapshotListener
                    //데이터 get
                    for(snapshot in querySnapshot.documents){
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    notifyDataSetChanged() //새로고침 되도록
                }
            }


            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
                var width = resources.displayMetrics.widthPixels / 3
                var imageview = ImageView(p0.context)
                imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
                return CustomViewHolder(imageview)
            }

            inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)


            override fun getItemCount(): Int {
                return contentDTOs.size
            }

            override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
                var imageview = (p0 as CustomViewHolder).imageview
                Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
            }
        }
}
