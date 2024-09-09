package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothDevice


data class SampleDeviceInfo(val n:String,val mDdr:String,val isConn:Boolean = false,var isPaired: Boolean = false,var device:BluetoothDevice? = null){

    var name: String = n
    var mac_addr : String = mDdr
    var is_conn: Boolean = isConn
    var is_paired:Boolean = isPaired
    var deviceObj: BluetoothDevice? = device

}