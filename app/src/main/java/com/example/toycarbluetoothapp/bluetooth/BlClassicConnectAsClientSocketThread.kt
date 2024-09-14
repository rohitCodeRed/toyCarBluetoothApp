package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.util.Log
import java.io.IOException
import java.util.UUID

class BlClassicConnectAsClientSocketThread () {

    private var thread:ConnectThread ? = null

    fun setThread(device:BluetoothDevice,uuid: UUID){
        println("Initializing thread...")
            thread = ConnectThread(device,uuid)
        if(thread == null){
            println("Thread not initilaize...")
        }
    }

    fun startThread(){
        println("Starting thread...")
        if(thread != null){
            println("Starting thread...in.....")
            thread!!.start()
        }
    }
    fun cancelThread(){

        var status = BlClassicDeviceServices.getBluetoothAdapter()?.cancelDiscovery()
        if(status != null && status){
            println("Discovery ended {cancelThread}")
        }else{
            println("Error in cancel discovery {cancelThread()}")
        }

        if(thread != null){
            if(thread!!.isAlive){
                thread?.cancel()
            }
        }

    }

    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID):Thread(){

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            println("Created RF comm Socket.....${uuid}: $device")
            device.createRfcommSocketToServiceRecord(uuid)
        }


        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            if(BlClassicDeviceServices.getBluetoothAdapter()?.isDiscovering == true){
                BlClassicDeviceServices.getBluetoothAdapter()?.cancelDiscovery()
            }

            try {
                println("In Connection thread....")
                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    //manageMyConnectedSocket(socket)
                    BlClassicDeviceServices.setClientSocket(socket)

                }
            }
            catch (e:Exception){
                BlClassicDeviceServices.sentSocketErrorMsg(e.message)
                println("Socket error :${e.message}\n")
            }


        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                //BluetoothDeviceServices.disconnectClientSocket()
            } catch (e: IOException) {
                BlClassicDeviceServices.sentSocketErrorMsg(e.message)
                //BluetoothDeviceServices.disconnectClientSocket()
                Log.e(TAG, "Could not close the client socket", e)
            }
        }

    }


}