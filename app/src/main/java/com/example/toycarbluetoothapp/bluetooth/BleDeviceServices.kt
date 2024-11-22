package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.toycarbluetoothapp.Constants
import java.util.UUID


class BLeDeviceServices: android.app.Service(){
    private val TAG = "BLeDeviceServices"
    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    var connectionState:Boolean = false

    fun initializeBlAdaptor(): Boolean {
        bluetoothAdapter = BluetoothDeviceListHelper.getAdaptor()
        if (bluetoothAdapter == null) {
            println("$TAG: Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }


    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                println("$TAG: Device not found with provided address. Unable to connect.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            println("$TAG: BluetoothAdapter not initialized..")
            return false
        }

    }

    fun disConnect(): Boolean{
        bluetoothAdapter?.let { adapter ->
            try {
                // connect to the GATT server on the device
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
                BluetoothDeviceListHelper.setBleDeviceConnectStatus(false)
                broadcastUpdate(ACTION_GATT_DISCONNECTED)

                return true
            } catch (exception: IllegalArgumentException) {
                println("$TAG: Not able to disConnect, something went wrong...")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            println("$TAG: BluetoothAdapter not initialized")
            return false
        }
    }


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            println("$TAG: Connection state found...  $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                println("$TAG: Connection to gatt server happened.....")
                connectionState = true
                BluetoothDeviceListHelper.setBleDeviceConnectStatus(true)
                broadcastUpdate(ACTION_GATT_CONNECTED)

                bluetoothGatt?.discoverServices()

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = false
                BluetoothDeviceListHelper.setBleDeviceConnectStatus(false)
                println("$TAG: Disconnection gatt server happened.....")
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("$TAG: Services discovered ...")
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                println("$TAG: No services discovered...")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data:ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("$TAG: Read characteristics happened..")
                broadCastUpdateWithData(ACTION_DATA_AVAILABLE, characteristic,data)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data:ByteArray
        ) {
            println("$TAG: Characteristic changed happened...\n")
            broadCastUpdateWithData(ACTION_DATA_AVAILABLE, characteristic,data)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
            if(status == BluetoothGatt.GATT_SUCCESS){
                val byte:Byte = 0
                if(value[0] != byte){
                    println("$TAG: Descriptor read successfully.... with Characteristic uuid : ${descriptor.getCharacteristic().uuid} \n")
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

            if(status == BluetoothGatt.GATT_SUCCESS){
                println("$TAG: Descriptor written successfully...")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("$TAG: BluetoothGatt write successfully...")
            }
        }

    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            gatt.readCharacteristic(characteristic)
        } ?: run {
            println("$TAG: BluetoothGatt not initialized...")
            return
        }
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)

            val value: ByteArray
            val properties = characteristic.properties
            value = if (properties and PROPERTY_NOTIFY > 0) {
                //println("$TAG: value: ${BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE}")
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else if (properties and PROPERTY_INDICATE > 0) {
                //println("$TAG: value: ${BluetoothGattDescriptor.ENABLE_INDICATION_VALUE}")
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            } else {
                println("$TAG: ERROR: Characteristic %s does not have notify or indicate property ${characteristic.uuid}")
                return
            }


            val descriptor = characteristic.getDescriptor(UUID.fromString(Constants.BLE_DEVICE_DESCRIPTOR_UUID))
            if(descriptor != null){
                println("$TAG: Description :uuid: ${descriptor.characteristic.uuid}")
                gatt.writeDescriptor(descriptor,value)
            }else{
                println("$TAG: Descriptor is null..")
            }

        } ?: run {
            println("$TAG: BluetoothGatt not initialized for notification...")
        }

    }


    fun writeCharacteristic(characteristic:BluetoothGattCharacteristic,bytesToWrite:ByteArray){

            bluetoothGatt?.writeCharacteristic(characteristic,bytesToWrite,characteristic.writeType)

    }


    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }


    private fun broadCastUpdateWithData(action: String, characteristic: BluetoothGattCharacteristic, data:ByteArray?){
        val intent = Intent(action)

        if (data?.isNotEmpty() == true) {
            intent.putExtra(Constants.DATA_KEY, data.toString(Charsets.UTF_8))
        }
        sendBroadcast(intent)
    }


    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"

    }



    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BLeDeviceServices {
            return this@BLeDeviceServices
        }
    }
}