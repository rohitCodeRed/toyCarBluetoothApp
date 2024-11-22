package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceInfo(val n:String?, val mDdr:String, var device: BluetoothDevice? = null, var isSel:Boolean = false,var classic:Boolean?=false,var ble:Boolean?=false) {

    var name: String? = n
    var addr: String = mDdr
    var deviceObj: BluetoothDevice? = device
    var isSelected:Boolean = isSel

    var classicType:Boolean? = classic
    var bleType:Boolean? = ble
}