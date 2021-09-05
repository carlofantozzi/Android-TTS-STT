package com.example.thesisproject

interface CallBackUpdate {

    fun onUpdate(result:String)

    fun onError(error:String)

    fun onFinished()

}