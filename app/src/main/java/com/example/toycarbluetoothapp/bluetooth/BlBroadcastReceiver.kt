package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BlBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val action:String? = intent?.getAction()
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            val state:Int? = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when(state){
                BluetoothAdapter.STATE_OFF -> {
                    println(" Bluetooth is turned OFF, please turn it on")
                    BlClassicDeviceServices.setBluetoothEnable(false)
                    BleDeviceListServices.setBluetoothStatus(false)
                    Toast.makeText(context, "Bluetooth is turned OFF, please turn it on", Toast.LENGTH_LONG).show()
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {}
                BluetoothAdapter.STATE_ON -> {
                    println(" Bluetooth is turned ON")
                    BlClassicDeviceServices.setBluetoothEnable(true)
                    BleDeviceListServices.setBluetoothStatus(true)
                    Toast.makeText(context, "Bluetooth is turned ON", Toast.LENGTH_LONG).show()

                }
                BluetoothAdapter.STATE_TURNING_ON ->{}
            }
        }
        else if(action.equals(BluetoothDevice.ACTION_FOUND)){
            println("Device reached at reciever point BluetoothDevice.ACTION_FOUND\n")
            val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            println("Device data  in ACTION FOUND: ${device}")
            println("Device data  in ACTION FOUND: ${device!!.name}")
            println("Device data  in ACTION FOUND: ${device.address}")
//            val deviceName = device?.name
//            val deviceHardwareAddress = device?.address

            //if(device!=null){
            try {
                BlClassicDeviceServices.addFoundBlDeviceInList(BlClassicDeviceInfo(device!!.name,device.address,false,false,device))
            }catch(e:Exception){
                println("Error occured in broadcast reciever: ${e.message}")
            }

            //}else{
               // println("Found Device is null")
           // }


        }

        else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
            BlClassicDeviceServices.setActionDiscovery(true)
            //println("Entering into action discovery started")

        }

//        else if(action.equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))){
//            BluetoothDeviceServices.setActionDiscovery(false)
//           // println("Entering into action discovery finished")
//        }

    }
}