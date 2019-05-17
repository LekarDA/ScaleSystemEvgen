package com.dmitriy.android.scalesystem

import android.content.Context
import android.hardware.usb.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dmitriy.android.scalesystem.usbserial.util.HexDump
import kotlinx.android.synthetic.main.activity_main.*

class ScaleActivity: AppCompatActivity(), View.OnClickListener, Listener {

    private val TAG = ScaleActivity::class.java.simpleName
    val DEVICE_NAME = "DIBAL S.A"

    var usbManager: UsbManager? = null
    var device:UsbDevice ? = null
    var usbInterface : UsbInterface? = null
    private var inEndpoint: UsbEndpoint? = null
    private var outEndpoint: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null
    private var ioManager: InputOutputManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_getValue.setOnClickListener(this)

        device = findDevice()
        initUsbDevice()
    }

    private fun findDevice(): UsbDevice? {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val devices = usbManager?.deviceList?.values
        devices?.forEach {
            if(it?.manufacturerName.equals(DEVICE_NAME))
                return it
        }
        return null
    }

    private fun initUsbDevice() {
        usbInterface = device?.getInterface(1)
        repeat(times = usbInterface?.endpointCount ?: 0) { index ->
            val tmpEndpoint = usbInterface?.getEndpoint(index)
//            if(tmpEndpoint?.type != UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (outEndpoint == null && tmpEndpoint?.direction == UsbConstants.USB_DIR_OUT) {
                    outEndpoint = tmpEndpoint
                } else if (inEndpoint == null && tmpEndpoint?.direction == UsbConstants.USB_DIR_IN) {
                    inEndpoint = tmpEndpoint
                }
//            }
        }

        outEndpoint ?: Toast.makeText(this, "no endpoints", Toast.LENGTH_LONG).show()

        connection = usbManager?.openDevice(device)
        connection ?: Toast.makeText(this, "can't open device", Toast.LENGTH_SHORT).show()


        connection?.claimInterface(usbInterface,true)
//        startIOmanager()
    }

    private fun startIOmanager() {
        Log.i(TAG, "Starting io manager ..")
        ioManager = InputOutputManager(inEndpoint,outEndpoint,connection,this)
    }

    override fun onPause() {
        ioManager?.stop()
        super.onPause()
    }

    private fun updateReceivedData(data: ByteArray) {
        val message = ("Read " + data.size + " bytes: \n" + HexDump.dumpHexString(data) + "\n\n")
        tvInfo.append(message)
    }

    override fun onClick(v: View?) {
        startIOmanager()
    }

    override fun onNewData(data: ByteArray) {
        updateReceivedData(data)
    }

    override fun onRunError(e: Exception) {
        Log.i("InputOutputManager", "Runner stopped.")
    }

}