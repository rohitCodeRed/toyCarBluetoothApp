package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.toycarbluetoothapp.Constants

object BlClassicDeviceServices {
    private var bluetoothAdapter:BluetoothAdapter? = null
    private var isDiscoveryStarted:Boolean = false
    private var isBluetoothEnable:Boolean = false
    private var isClientDevConn:Boolean = false
    private var isPermissionGranted:Boolean = false
    var connDeviceInfo:BlClassicDeviceInfo? = null
    private val pairedDeviceList:MutableList<BlClassicDeviceInfo> = mutableListOf()
    private var allAvailableDevices:MutableList<BlClassicDeviceInfo> = mutableListOf()
    private var bluetoothClientSocket:BluetoothSocket? = null
    private var bluetoothServerSocket:BluetoothSocket? = null
    private  var activityHandler:Handler? = null

    private var socketCreationClass:BlClassicConnectAsClientSocketThread? = null




    fun getActivityHandler():Handler?{
        return activityHandler
    }
    fun setActionDiscovery(s:Boolean){
        isDiscoveryStarted = s
        if(s){
            activityHandler?.obtainMessage(Constants.DISCOVERY_MODE, Constants.START)!!.sendToTarget()
        }
        else{
            activityHandler?.obtainMessage(Constants.DISCOVERY_MODE, Constants.END)!!.sendToTarget()
        }
    }
    fun setSocketCreateClass(s:BlClassicConnectAsClientSocketThread?){
        socketCreationClass = s
    }

    fun getSocketCreateClass():BlClassicConnectAsClientSocketThread?{
       return socketCreationClass
    }


    fun scanBluetoothPairedDevice(){
        if(!isPermissionGranted){
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device!!.name
            val deviceHardwareAddress = device.address // MAC address

            if(!checkDeviceIsAlreadyPresent(deviceHardwareAddress)){
                if(checkDeviceIsConnected(device) == true){
                    allAvailableDevices.add(BlClassicDeviceInfo(deviceName,deviceHardwareAddress,true,true,device))
                }
                else{
                    allAvailableDevices.add(BlClassicDeviceInfo(deviceName,deviceHardwareAddress,false,true,device))
                }

            }

        }
        if(null != pairedDevices){
            activityHandler?.obtainMessage(Constants.DEVICE_FOUND_UPDATE, Constants.WHOLE_DEVICE_LIST)!!.sendToTarget()
        }
    }

    private fun checkDeviceIsAlreadyPresent(mac_addr:String):Boolean{
        var device = allAvailableDevices.find { it.mac_addr == mac_addr }
        if(null == device){
            return false
        }
        return true
    }
    fun getBluetoothAdapter():BluetoothAdapter?{
        return bluetoothAdapter
    }

    fun setBluetoothAdapter(adapter:BluetoothAdapter){
        this.bluetoothAdapter = adapter
    }

    fun addFoundBlDeviceInList(device:BlClassicDeviceInfo){
        if(!checkDeviceIsAlreadyPresent(device.mac_addr)) {
            allAvailableDevices.add(device)
            activityHandler?.obtainMessage(Constants.DEVICE_FOUND_UPDATE, Constants.ONE_DEVICE)!!
                .sendToTarget()
        }
    }

    fun clearAllAvailableDevices(){
        allAvailableDevices.removeAll(allAvailableDevices)
    }

    fun getConnectedBlDevices():MutableList<BlClassicDeviceInfo>{
        var devices:MutableList<BlClassicDeviceInfo> = mutableListOf()
        devices.addAll(allAvailableDevices.filter { it.is_conn })
        //devices.addAll(pairedDeviceList.filter { it.is_conn })
        return devices
    }

    fun getDeviceByAddress(addr:String):BluetoothDevice?{
        var dev = allAvailableDevices.find { it.mac_addr ==  addr}
        if(dev != null){
            return dev.deviceObj
        }
        return null
    }

    fun getAllAvailableDevice():MutableList<BlClassicDeviceInfo>{
        sortAllAvaialbleDevice()
        return allAvailableDevices
    }

    fun startDiscoverable(activity: AppCompatActivity,code:Int,bundle:Bundle,duration:Int = 100){
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        startActivityForResult(activity,discoverableIntent,code, bundle)
    }



    fun setBluetoothEnable(s:Boolean){
        this.isBluetoothEnable = s
        if(s){
            activityHandler?.obtainMessage(Constants.BL_HANDLER,1,)?.sendToTarget()
        }else{
            activityHandler?.obtainMessage(Constants.BL_HANDLER,0,)?.sendToTarget()
        }
    }

    fun getBluetoothEnStatus():Boolean{
        return  this.isBluetoothEnable
    }

    fun setClientDevConn(s:Boolean){
        this.isClientDevConn = s
    }

    fun setPermissionGrant(s:Boolean){
        this.isPermissionGranted = s
//        if(s){
//            activityHandler?.obtainMessage(Constants.PERMISSION_HANDLER,1,)?.sendToTarget()
//        }else{
//            activityHandler?.obtainMessage(Constants.PERMISSION_HANDLER,0,)?.sendToTarget()
//        }
    }
    fun getPermission():Boolean{
        return this.isPermissionGranted
    }


    fun setServerSocket(socket:BluetoothSocket){
        bluetoothServerSocket = socket
    }

    fun getServerSocket():BluetoothSocket?{
        return bluetoothServerSocket
    }

    fun closeServerSocket(){
        bluetoothServerSocket?.close()
        bluetoothServerSocket = null
    }


    fun setClientSocket(socket:BluetoothSocket?){
        if(socket != null){
            bluetoothClientSocket = socket
            connDeviceInfo = BlClassicDeviceInfo(socket.remoteDevice.name,socket.remoteDevice.address,true,checkDeviceIsPaired(socket.remoteDevice),socket.remoteDevice)
            setDeviceConnTrue(socket.remoteDevice)
            sortAllAvaialbleDevice()
            activityHandler?.obtainMessage(Constants.CLIENT_SOCKET, Constants.CONNECT)?.sendToTarget()
            return
        }

        bluetoothClientSocket = null
        connDeviceInfo = null
        activityHandler?.obtainMessage(Constants.CLIENT_SOCKET, Constants.DIS_CONNECT)?.sendToTarget()

       }

    fun getClientSocket():BluetoothSocket?{
        return bluetoothClientSocket
    }

    fun disconnectClientSocket(){
        setAllDeviceConnFalse()
        bluetoothClientSocket?.close()
        bluetoothClientSocket = null
        connDeviceInfo = null
        activityHandler?.obtainMessage(Constants.CLIENT_SOCKET, Constants.DIS_CONNECT)?.sendToTarget()
    }

    fun isSocketConnect():Boolean{
        if(bluetoothClientSocket != null){
            return bluetoothClientSocket!!.isConnected
        }
        return false
    }

    fun checkDeviceIsPaired(device:BluetoothDevice):Boolean{
        var tDev = allAvailableDevices.find { it.mac_addr == device.address }
        if(tDev != null){
            return tDev.is_paired
        }
        return false
    }

    fun checkDeviceIsConnected(device:BluetoothDevice):Boolean?{
        if(bluetoothClientSocket?.isConnected == true){
            var tDev = allAvailableDevices.find { it.mac_addr == device.address }
            if(tDev != null){
                return tDev.is_conn
            }
            if(connDeviceInfo?.mac_addr == device.address){
                return connDeviceInfo?.is_conn
            }
        }

        return false
    }

    fun setDeviceConnTrue(device:BluetoothDevice){
        allAvailableDevices.forEach {
            if(it.mac_addr == device.address){
                it.is_conn = true
            }
        }

    }

    fun setAllDeviceConnFalse(){
        allAvailableDevices.forEach {
            it.is_conn = false
        }

    }

    fun sortAllAvaialbleDevice(){
        allAvailableDevices.sortedBy { it.is_conn }
    }


    fun setActivityHandler(h:Handler){
        if(activityHandler != null){
            activityHandler = null
            activityHandler = h
            return
        }

        activityHandler = h
    }

    fun sentSocketErrorMsg(m:String?){
        activityHandler?.obtainMessage(Constants.CLIENT_SOCKET_ERROR,m)?.sendToTarget()
    }


}