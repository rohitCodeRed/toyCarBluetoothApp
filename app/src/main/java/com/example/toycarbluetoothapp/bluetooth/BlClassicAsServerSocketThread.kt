package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.util.Log
import java.io.IOException

private class BluetoothAsServerSocket(name:String, uuid:java.util.UUID) : Thread() {

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        BlClassicDeviceServices.getBluetoothAdapter()?.listenUsingInsecureRfcommWithServiceRecord(name, uuid)
    }

    override fun run() {
        // Keep listening until exception occurs or a socket is returned.
        var shouldLoop = true
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                Log.e(TAG, "Socket's accept() method failed", e)
                shouldLoop = false
                null
            }
            socket?.also {
                BlClassicDeviceServices.setServerSocket(it)
                //manageMyConnectedSocket(it)
                mmServerSocket?.close()
                shouldLoop = false
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    fun cancel() {
        try {
            mmServerSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}