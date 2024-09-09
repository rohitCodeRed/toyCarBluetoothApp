package com.example.toycarbluetoothapp.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ControlViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text
//
//    val recieveData:MutableLiveData<String> = MutableLiveData<String>().apply {
//        value = "10% duty Cycle"
//    }

    val recieveSpeed:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "00 : 00"
    }

    val deviceName:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "Device: XX"
    }

    val deviceAddress:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "Addr: YY"
    }

    val r_btn_obstacle:MutableLiveData<Boolean> =  MutableLiveData<Boolean>().apply {
        value = false
    }

    val startBtn:MutableLiveData<Boolean> =  MutableLiveData<Boolean>().apply {
        value = false
    }

}