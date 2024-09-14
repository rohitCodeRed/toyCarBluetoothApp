package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.UUID

class BlClassicConnectAsClientSocketThread () {
    private var TAG:String="BlClassicConnectAsClientSocketThread"
    private var thread:ConnectThread ? = null

    fun setThread(device:BluetoothDevice,uuid: UUID){
        thread = ConnectThread(device,uuid)
    }

    fun startThread(){
        if(thread != null){
            thread!!.start()
        }
    }
    fun cancelThread(){
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
            if(BluetoothDeviceListHelper.getAdaptor()?.isDiscovering == true){
                BluetoothDeviceListHelper.getAdaptor()?.cancelDiscovery()
            }

            try {
                println("In client socket connection thread....")
                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    //manageMyConnectedSocket(socket)
                    BluetoothDeviceListHelper.setClientSocket(socket)

                }
            }
            catch (e:Exception){
                BluetoothDeviceListHelper.disconnectClientSocket()
                println("Socket error :${e.message}\n")
            }


        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                BluetoothDeviceListHelper.disconnectClientSocket()
            } catch (e: IOException) {
                BluetoothDeviceListHelper.disconnectClientSocket()
                Log.e(TAG, "Could not close the client socket", e)
            }
        }

    }


}