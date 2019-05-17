package com.dmitriy.android.scalesystem

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.support.annotation.UiThread
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer

class InputOutputManager(val inEndpoint: UsbEndpoint?,val outEndpoint: UsbEndpoint?,
                         val connection: UsbDeviceConnection?,val listener: Listener) : ReadData{


    private val BUFSIZ = 4096
    private val READ_TIMEOUT = 1000
    private val mReadBuffer = ByteBuffer.allocate(BUFSIZ)

    var scannerJob: Job? = null
    var secondJob: Job ? = null


    init {
        secondJob = GlobalScope.launch(Dispatchers.Main) {
            startScaning()
        }

    }

    suspend private fun startScaning() {
        scannerJob = GlobalScope.launch(Dispatchers.IO) {
            while (this.isActive){
                val data = readScannerData()
                data?.let(::updateUI)
            }
        }
    }


    override suspend fun readScannerData(): Int? {
        try {
           return read(mReadBuffer.array())
        }catch (e :Exception) {
            listener.onRunError(e)
        }
        return -1
    }

    @Throws(IOException::class)
    fun read(data: ByteArray): Int? {
        val packageSize = inEndpoint?.maxPacketSize?: return null
        val size = Math.min(data.size, packageSize)
        return connection?.bulkTransfer(inEndpoint, data, size, READ_TIMEOUT)
    }

    @UiThread
    fun updateUI(readData : Int){
        if (readData > 0) {
            val data = ByteArray(readData )
            mReadBuffer.get(data, 0, readData)
            listener.onNewData(data)
            mReadBuffer.clear()
        }
    }

    fun stop(){
        scannerJob?.cancel()
        secondJob?.cancel()
    }
}