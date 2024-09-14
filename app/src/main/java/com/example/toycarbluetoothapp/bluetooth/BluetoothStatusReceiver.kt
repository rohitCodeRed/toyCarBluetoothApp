package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BluetoothStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val action:String? = intent?.getAction()
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            val state:Int? = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when(state){
                BluetoothAdapter.STATE_OFF -> {
                    BluetoothDeviceListHelper.setBluetoothStatus(false)
                    Toast.makeText(context, "Bluetooth is turned OFF, please turn it on", Toast.LENGTH_LONG).show()
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {}
                BluetoothAdapter.STATE_ON -> {
                    BluetoothDeviceListHelper.setBluetoothStatus(true)
                    Toast.makeText(context, "Bluetooth is turned ON", Toast.LENGTH_LONG).show()

                }
                BluetoothAdapter.STATE_TURNING_ON ->{}
            }
        }



    }
}