package com.example.offlinespeech

interface CallBackUpdate {

    fun onUpdate(result:String)

    fun onError(error:String)

    fun onFinished()

}