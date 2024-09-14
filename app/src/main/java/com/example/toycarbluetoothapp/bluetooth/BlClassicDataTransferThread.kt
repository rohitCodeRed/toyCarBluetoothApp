package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.toycarbluetoothapp.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class BluetoothDataTransferThread () {

    private  var thread: ConnectThread? = null

    fun setThread(mmSocket: BluetoothSocket,mHandler: Handler) {
        thread = ConnectThread(mmSocket,mHandler)
    }

    fun getThreadStatus():Boolean?{
        if(thread != null){
            return thread?.isAlive
        }
        return false
    }

    fun startThread() {
        if(thread != null){
            cancelThread()
            thread!!.start()
        }

    }

    fun sendCommand(s:String){
        try {
            if(thread!!.isAlive && BluetoothDeviceListHelper.getClientSocket() != null){
                thread!!.write(s.toByteArray())

            }
        }
        catch(e:Exception){
            println("Not able send data : error - ${e.message}")
        }

    }

    fun cancelThread() {
        if(thread != null){
            if(thread!!.isAlive){
                thread!!.cancel()
            }
        }
    }

    private inner class ConnectThread(pSocket: BluetoothSocket,pHandler:Handler): Thread() {
        val TAG:String="Connected thread"
        private val mmSocket :BluetoothSocket = pSocket
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)
        private val mHandler:Handler = pHandler

        override fun run() {
            var numBytes: Int // bytes returned from read()
            mHandler.obtainMessage(
                Constants.CONNECT,"Data transfer started...").sendToTarget()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream is disconnected", e)
                    mHandler.obtainMessage(
                        Constants.CONNECTION_ERROR,e.localizedMessage).sendToTarget()
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = mHandler.obtainMessage(
                    Constants.MESSAGE_READ, numBytes, -1,
                    mmBuffer.toString(Charsets.UTF_8))
                readMsg.sendToTarget()
            }
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                mHandler.obtainMessage(
                    Constants.CONNECTION_ERROR,e.localizedMessage).sendToTarget()

                // Send a failure message back to the activity.
//                val writeErrorMsg:Message = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("Toast", "Couldn't send data to the other device")
//                }
//                writeErrorMsg.data = bundle
//                mHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = mHandler.obtainMessage(
                Constants.MESSAGE_WRITE, -1, -1, bytes.decodeToString())
            writtenMsg.sendToTarget()
        }

        fun cancel() {
            try {
                mmSocket.close()
                mHandler.obtainMessage(
                    Constants.CONNECTION_STOP,"Cancel btn called..").sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
                mHandler.obtainMessage(
                    Constants.CONNECTION_ERROR,"Cancel btn called. and error occur ${e.message}").sendToTarget()
            }
        }

    }

}
