package com.dmitriy.android.scalesystem

import android.content.Context
import android.hardware.usb.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.dmitriy.android.scalesystem.usbserial.driver.*
import java.io.IOException
import com.dmitriy.android.scalesystem.usbserial.driver.CdcAcmSerialDriver
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {


    val DEVICE_NAME = "DIBAL S.A"

    var port: UsbSerialPort? = null
    var driver: UsbSerialDriver? = null
    var usbManager: UsbManager? = null
    var availableDrivers: MutableList<UsbSerialDriver>? = null
    var connection:UsbDeviceConnection? = null

    private val TIMEOUT = 3000
    private val forceClaim = true
    var readBytes = ByteArray(64)
    var usbInterface: UsbInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_getValue.setOnClickListener(this)
    }


    override fun onResume() {
        super.onResume()
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices = usbManager?.deviceList?.values

        devices?.forEach {
                device -> Log.i("device_Dibal", device?.manufacturerName + " device class: " + device.deviceClass.toString())
            if(device?.manufacturerName?.equals(DEVICE_NAME)!!){
                /*var usbInterface*/usbInterface = device.getInterface(1)
                Log.i("device_Dibal", "count of endpoints: "+ usbInterface?.endpointCount.toString())
                connection = usbManager?.openDevice(device)
                connection?.claimInterface(usbInterface, forceClaim)
                if(usbInterface?.getEndpoint(1)?.direction == UsbConstants.USB_DIR_IN) Log.i("device_Dibal","direction in")
                else if(usbInterface?.getEndpoint(1)?.direction == UsbConstants.USB_DIR_OUT)Log.i("device_Dibal","direction out")
                else Log.i("device_Dibal","direction not found")
                    getValue()
//                var answer = connection?.bulkTransfer(usbInterface?.getEndpoint(1),readBytes,readBytes.size,TIMEOUT)
//                Log.e("device_Dibal"," answer: " + answer)
            }
        }
    }

    override fun onClick(v: View?) {
        getValue()
    }


    fun getValue(){
        var answer = connection?.bulkTransfer(usbInterface?.getEndpoint(1),readBytes,readBytes.size,TIMEOUT)
        Log.e("device_Dibal"," answer: " + answer)
    }
}
