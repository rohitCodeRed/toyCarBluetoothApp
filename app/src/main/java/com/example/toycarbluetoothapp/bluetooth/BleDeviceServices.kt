package com.example.toycarbluetoothapp.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_SIGNED
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
    private val EXTRA_DATA = "CHARAS_DATA"
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
                println("$TAG: Device not found with provided address.  Unable to connect.")
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            println("$TAG: BluetoothAdapter not initialized")
            return false
        }

    }

    fun disConnect(): Boolean{
        bluetoothAdapter?.let { adapter ->
            try {
                // connect to the GATT server on the device
                bluetoothGatt?.close()
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
                // successfully connected to the GATT Server
                connectionState = true
                broadcastUpdate(ACTION_GATT_CONNECTED)
                println("$TAG: Connect to gatt server happened.....")
                bluetoothGatt?.discoverServices()

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = false
                println("$TAG: Disconnection gatt server happened.....")
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("$TAG: onServicesDiscovered received: $status")
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
                broadCastUpdateWithData(ACTION_DATA_AVAILABLE, characteristic,data)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data:ByteArray
        ) {
            broadCastUpdateWithData(ACTION_DATA_AVAILABLE, characteristic,data)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //todo
                println("$TAG: BluetoothGatt write succesffully...")
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
            println("$TAG: BluetoothGatt not initialized")
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
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else if (properties and PROPERTY_INDICATE > 0) {
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            } else {
                println("$TAG: ERROR: Characteristic %s does not have notify or indicate property ${characteristic.uuid}")

                return
            }

            val descriptor = characteristic.getDescriptor(UUID.fromString(Constants.CCC_UUID))
            //descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor,value)

            // This is specific to Heart Rate Measurement.
//            if (BluetoothLeService.UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
//                val descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
//                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                gatt.writeDescriptor(descriptor)
//            }
        } ?: run {
            println("$TAG: BluetoothGatt not initialized")
        }

    }


    fun writeCharacteristics(characteristic:BluetoothGattCharacteristic,bytesToWrite:ByteArray){
        var flag = 0
        var correctType:Int = 0

        Constants.POSSIBLE_WRITE_TYPES.forEach {
            var writeProperty: Int = 0
            when(it){
                WRITE_TYPE_DEFAULT ->{
                    writeProperty = PROPERTY_WRITE
                }

                WRITE_TYPE_NO_RESPONSE->{
                    writeProperty = PROPERTY_WRITE_NO_RESPONSE
                }

                WRITE_TYPE_SIGNED->{
                    writeProperty = PROPERTY_SIGNED_WRITE
                }

            }

            if((characteristic.properties and writeProperty) == 0 ) {
                println("$TAG: ERROR: Characteristic ${characteristic.uuid} does not support writeType ${it}")
                //return
            }else{
                correctType = it
            }
        }

        if(correctType == 0){
            println("$TAG: ERROR: Not able to find correct types...")
            return
        }

        bluetoothGatt?.writeCharacteristic(characteristic,bytesToWrite,correctType)

    }




    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadCastUpdateWithData(action: String, characteristic: BluetoothGattCharacteristic, data:ByteArray?){
        val intent = Intent(action)

        if (data?.isNotEmpty() == true) {
            val hexString: String = data.joinToString(separator = " ") {
                String.format("%02X", it)
            }
            intent.putExtra(EXTRA_DATA, "$data\n$hexString")
        }

        sendBroadcast(intent)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        /*when (characteristic.uuid) {
            UUID_HEART_RATE_MEASUREMENT -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        Log.d(TAG, "Heart rate format UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        Log.d(TAG, "Heart rate format UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.

                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                }
            }
        }*/
        //sendBroadcast(intent)
    }


    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"

        private const val STATE_DISCONNECTED = 1
        private const val STATE_CONNECTED = 2

    }



    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        println("$TAG: On bind happened..")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BLeDeviceServices {

            println("$TAG: Get service called....")
            return this@BLeDeviceServices
        }
    }
}