package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceInfo(val n:String?, val mDdr:String, var device: BluetoothDevice? = null, var isSel:Boolean = false) {

    var name: String? = n
    var addr: String = mDdr
    var deviceObj: BluetoothDevice? = device
    var isSelected:Boolean = isSel
}