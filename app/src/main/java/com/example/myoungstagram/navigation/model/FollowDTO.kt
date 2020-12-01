package com.example.myoungstagram.navigation.model

data class FollowDTO(
    var followerCount : Int = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),
    //중복을 방지하기위해 맵을 만듬
    var followingCount : Int =0,
    var followings : MutableMap<String,Boolean> = HashMap()
)