package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import com.example.toycarbluetoothapp.Constants

object BluetoothDeviceListHelper {
    private val TAG = "BleDeviceListServices"
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var isDiscoveryStarted:Boolean = false
    private var isBluetoothEnable:Boolean = false
    private var isBleDeviceConnected:Boolean = false
    private var isClassicDeviceConnected:Boolean = false

    private var isPermissionGranted:Boolean = false
    var selectedDeviceInfo:BluetoothDeviceInfo? = null
    private var allDevice:MutableList<BluetoothDeviceInfo> = mutableListOf()
    private  var activityHandler: Handler? = null

    private var bleServiceClassRef:BLeDeviceServices? = null

    private var socketCreationClass:BlClassicConnectAsClientSocketThread? = null
    private var bluetoothClientSocket: BluetoothSocket? = null



    fun setClientSocket(socket:BluetoothSocket?){
        if(socket != null){
            bluetoothClientSocket = socket

            activityHandler?.obtainMessage(Constants.CLIENT_SOCKET, Constants.CONNECT)?.sendToTarget()
            return
        }

        bluetoothClientSocket = null
    }

    fun getClientSocket():BluetoothSocket?{
        return bluetoothClientSocket
    }

    fun disconnectClientSocket(){

        bluetoothClientSocket?.close()
        bluetoothClientSocket = null

       activityHandler?.obtainMessage(Constants.CLIENT_SOCKET, Constants.DIS_CONNECT)?.sendToTarget()
    }

    fun setSocketCreateClass(s:BlClassicConnectAsClientSocketThread?){
        socketCreationClass = s
    }

    fun getSocketCreateClass():BlClassicConnectAsClientSocketThread?{
        return socketCreationClass
    }

    fun isBleDeviceConnected():Boolean{
        return isBleDeviceConnected
    }

    fun isClassicDeviceConnected():Boolean{
        return isClassicDeviceConnected
    }

    fun setBleDeviceConnectStatus(b:Boolean){
        isBleDeviceConnected = b
    }

    fun setClassicDeviceConnectStatus(b:Boolean){
        isClassicDeviceConnected = b
    }

    fun getBleServiceClass():BLeDeviceServices?{
        return bleServiceClassRef
    }

    fun setBleServiceClass(c:BLeDeviceServices?){
        if(c != null){
            bleServiceClassRef = c
            println("$TAG: Ble service class updated...")
            return
        }
        bleServiceClassRef = null

    }

    fun setBluetoothStatus(s:Boolean){
        isBluetoothEnable = s
    }

    fun setPermissionStatus(s:Boolean){
        isPermissionGranted = s
    }

    fun getPermissionStatus():Boolean{
        return isPermissionGranted
    }

    fun getBluetoothStatus():Boolean{
        return isBluetoothEnable
    }


    fun setAdaptor(a:BluetoothAdapter?){
        if(a != null){
            bluetoothAdapter = a
        }
    }

    fun getAdaptor():BluetoothAdapter? {
        return bluetoothAdapter
    }

    fun getActivityHandler():Handler?{
        return activityHandler
    }

    fun setActivityHandler(h:Handler?){
        if(h != null){
            activityHandler = null
            activityHandler = h
        }
    }

    fun getDeviceList():MutableList<BluetoothDeviceInfo>{
        return allDevice
    }


    fun scanBluetoothPairedDevice():MutableList<BluetoothDeviceInfo>{
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices
        pairedDevices?.forEach { device ->
            addDevice(device)
        }
        if(allDevice.isNotEmpty()){
            return allDevice
        }
        return mutableListOf()
    }


    fun addDevice(device:BluetoothDevice?):Boolean{

       if((device != null) &&(device?.name != null) && (device.address != null) ){
           if(!isDeviceAlreadyPresent(device.address)){
               allDevice.add(BluetoothDeviceInfo(device.name,device.address,device,false))
               println("$TAG: Device found  IN: ${device.name}")
               println("$TAG: Device found IN: ${device.address}")
               return true
           }
       }
        return false
    }

    fun isDeviceAlreadyPresent(a:String):Boolean{
        val dev = getDeviceList().find { it.addr ==  a}
        return dev != null
    }

    fun clearAllAvailableDevices(){
       allDevice.removeAll(allDevice)
    }

    fun notifyDataSetChanged(){

    }

    fun getDeviceByAddress(addr:String):BluetoothDeviceInfo?{
        val dev = getDeviceList().find { it.addr ==  addr}
        if(dev != null){
            return dev
        }
        return null
    }

    fun selectDevice(d:BluetoothDeviceInfo){
        if(selectedDeviceInfo != null){
            deSelectDevice(d)
        }

        allDevice.forEach {
            if (it.addr == d.addr) {
                it.isSelected = true
                selectedDeviceInfo = it
            }
        }

        sortDeviceList()
    }

    fun deSelectDevice(d:BluetoothDeviceInfo){

        allDevice.forEach {
            if (it.addr == d.addr) {
                it.isSelected = false
                selectedDeviceInfo = null
            }
        }
    }

    fun sortDeviceList(){
        allDevice.sortedBy { it.isSelected }
    }




}