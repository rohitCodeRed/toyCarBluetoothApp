package com.example.toycarbluetoothapp

import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_SIGNED


object Constants {

    // Message types sent from the BluetoothChatService Handler
    val MESSAGE_STATE_CHANGE:Int = 1
    val MESSAGE_READ:Int = 2
    val MESSAGE_WRITE:Int = 3
    val MESSAGE_TOAST:Int = 8
    val MSG_KEY:String ="Toast"
    val MESSAGE_DEVICE_NAME:Int = 4

    val INVALID_UUID:Int = 12

    // Key names received from the BluetoothChatService Handler
    val PERMISSION_HANDLER:Int = 4
    val BL_HANDLER:Int = 5

    val DEVICE_FOUND_UPDATE:Int = 6
    val ONE_DEVICE:Int = 0
    val WHOLE_DEVICE_LIST:Int = 1

    var CLIENT_UUID:String="00001101-0000-1000-8000-00805F9B34FB"
    //val CLIENT_UUID:String=(0xFFE0).toString()

    val CLIENT_SOCKET:Int = 7
    val CLIENT_SOCKET_ERROR= 11
    val CONNECT:Int = 1
    val DIS_CONNECT:Int = 2
    val CREATE_AND_CONNECT:Int = 3
    val NOT_CREATE_AND_CONNECT:Int = 2


    val DEVICE_READY_FOR = 9
    val DATA_TRANSFER = 1

    val CONNECTION_STOP:Int = 9
    val CONNECTION_ERROR:Int = 11

    val DISCOVERY_MODE:Int = 10
    val START=1
    val END= 0


    val HOME_FRAGMENT:String = "CLASSIC"
    val BLE_FRAGMENT:String = "BLE"
    val DEVICE_INFO:String = "OTHER"
    val FRAGMENT_FLAG_ADD:Int = 0
    val FRAGMENT_FLAG_REPLACE:Int =1

    val VISIBLE_FRAGMENT:HashMap<String,Boolean> = hashMapOf()

    val BLE_SCAN_PERIOD:Long = 5000

    fun getServiceAttr():HashMap<String,String>{
        val s1:HashMap<String,String> = hashMapOf()
        s1.put("name","AT-09")
        s1.put("uuid","")
        s1.put("charas","")
        return s1
    }



    val wtiteType:Int = WRITE_TYPE_NO_RESPONSE

    var BLE_DEVICE_SERVICE_UUID:String = "0000ffe0-0000-1000-8000-00805f9b34fb"
    var BLE_DEVICE_DESCRIPTOR_UUID_1:String = "00002901-0000-1000-8000-00805f9b34fb"
    val POSSIBLE_WRITE_TYPES:Array<Int> = arrayOf(WRITE_TYPE_DEFAULT,WRITE_TYPE_NO_RESPONSE,WRITE_TYPE_SIGNED)



    var BLE_DEVICE_CHARACTERISTIC_UUID:String = "0000ffe1-0000-1000-8000-00805f9b34fb"
    var BLE_DEVICE_DESCRIPTOR_UUID:String = "00002902-0000-1000-8000-00805f9b34fb"
    var DATA_KEY:String = "characteristicsData"

    fun updateBleUuids(s:String,c:String,d:String){
        BLE_DEVICE_SERVICE_UUID = s
        BLE_DEVICE_CHARACTERISTIC_UUID = c
        BLE_DEVICE_DESCRIPTOR_UUID = d
    }

    fun updateClassicUuid(c:String){
        CLIENT_UUID = c
    }

}
