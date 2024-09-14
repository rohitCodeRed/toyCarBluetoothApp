package com.example.toycarbluetoothapp.ui.ble

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Device: XX"
    }
    val text: LiveData<String> = _text

    val recieveSpeed_M1_M2:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "00 : 00"
    }

    val recieveSpeed_M3_M4:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "00 : 00"
    }

    val deviceName:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "Device: XX"
    }

    val deviceAddress:MutableLiveData<String> =  MutableLiveData<String>().apply {
        value = "Addr: YY"
    }

    val alertInd:MutableLiveData<Boolean> =  MutableLiveData<Boolean>().apply {
        value = false
    }

    val startBtn:MutableLiveData<Boolean> =  MutableLiveData<Boolean>().apply {
        value = false
    }
}