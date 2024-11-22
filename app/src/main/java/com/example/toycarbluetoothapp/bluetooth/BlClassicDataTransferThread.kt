package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.toycarbluetoothapp.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class BluetoothDataTransferThread () {
    private val TAG:String="BluetoothDataTransferThread"
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

        private val mmSocket :BluetoothSocket = pSocket
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private var mmBuffer: ByteArray = ByteArray(13)
        private var mmBuffer1: ByteArray = ByteArray(13)
        private val mHandler:Handler = pHandler
        private var messageRecieved:Boolean = false

        override fun run() {
            var numBytes: Int // bytes returned from read()
            mHandler.obtainMessage(
                Constants.CONNECT,"Data transfer started...").sendToTarget()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                try {
                    var sChar:Char = mmInStream.read().toChar()
                    println("Reading character: $sChar")
                    if(sChar =='s'){
                        for(num in 0..<mmBuffer.count()){
                            var eChar:Char = mmInStream.read().toChar()
                            println("storing message: $eChar")

                            if(eChar == 'e'){
                                messageRecieved = true;
                                break
                            }

                            mmBuffer[num] = eChar.code.toByte()
                        }
                    }
                }catch(e:IOException){
                    Log.d(TAG, "Input stream is disconnected", e)
                    mHandler.obtainMessage(
                        Constants.CONNECTION_ERROR,e.localizedMessage).sendToTarget()
                    break
                }

                if(messageRecieved){
                    val readMsg = mHandler.obtainMessage(
                        Constants.MESSAGE_READ, mmBuffer.count(), -1,
                        mmBuffer.toString(Charsets.UTF_8))
                    readMsg.sendToTarget()

                    messageRecieved = false;
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                mHandler.obtainMessage(
                    Constants.CONNECTION_ERROR,e.localizedMessage).sendToTarget()
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
