package com.example.toycarbluetoothapp.bluetooth


object Constants {

    // Message types sent from the BluetoothChatService Handler
    val MESSAGE_STATE_CHANGE:Int = 1
    val MESSAGE_READ:Int = 2
    val MESSAGE_WRITE:Int = 3
    val MESSAGE_TOAST:Int = 8
    val MSG_KEY:String ="Toast"
    val MESSAGE_DEVICE_NAME:Int = 4

    // Key names received from the BluetoothChatService Handler
    val PERMISSION_HANDLER:Int = 4
    val BL_HANDLER:Int = 5

    val DEVICE_FOUND_UPDATE:Int = 6
    val ONE_DEVICE:Int = 0
    val WHOLE_DEVICE_LIST:Int = 1

    val CLIENT_UUID:String="00001101-0000-1000-8000-00805F9B34FB"

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


}
