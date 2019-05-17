package com.dmitriy.android.scalesystem

interface Listener {
    fun onNewData(data: ByteArray)
    fun onRunError(e:Exception)
}