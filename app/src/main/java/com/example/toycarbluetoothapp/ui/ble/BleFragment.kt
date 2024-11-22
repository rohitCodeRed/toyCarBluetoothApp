package com.example.toycarbluetoothapp.ui.ble

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.toycarbluetoothapp.Constants
import com.example.toycarbluetoothapp.R
import com.example.toycarbluetoothapp.bluetooth.BLeDeviceServices
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceListHelper
import com.example.toycarbluetoothapp.databinding.FragmentBleBinding
import android.view.View.OnClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.toycarbluetoothapp.ui.bottomsheet.BleUpdateUuid
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BleFragment : Fragment(), OnClickListener{
    private val TAG = "BLeFragmentActivity"
    private var _binding: FragmentBleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var mContext: Context? =null
    private lateinit var bleViewModel: BleViewModel
    private var bluetoothService : BLeDeviceServices? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mContext = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bleViewModel = ViewModelProvider(this).get(BleViewModel::class.java)

        _binding = FragmentBleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initDataViewModel()

        initBtnOnClick()

        updateDeviceInfo()

        updateStartBtn()

        return root
    }

    private fun initRegisterReciever() {
        registerReceiver(mContext!!,gattUpdateReceiver, makeGattUpdateIntentFilter()!!,
            ContextCompat.RECEIVER_EXPORTED
        )
    }



    private fun initDataViewModel(){
        val deviceName: TextView = binding.bleDeviceName;
        bleViewModel.deviceName.observe(viewLifecycleOwner) {
            deviceName.text = it
        }

        val deviceAddr: TextView = binding.bleAddr;
        bleViewModel.deviceAddress.observe(viewLifecycleOwner) {
            deviceAddr.text = it
        }

        val recieveSpeedM1M2: TextView = binding.bleM1M2Value;
        bleViewModel.recieveSpeed_M1_M2.observe(viewLifecycleOwner) {
            recieveSpeedM1M2.text = it
        }

        val recieveSpeedM3M4: TextView = binding.bleM3M4Value;
        bleViewModel.recieveSpeed_M3_M4.observe(viewLifecycleOwner) {
            recieveSpeedM3M4.text = it
        }

        val alertInd: RadioButton = binding.bleAlert;
        bleViewModel.alertInd.observe(viewLifecycleOwner) {
            alertInd.isChecked = it
        }

        val startBtn: ImageButton = binding.bleStartBtn
        bleViewModel.startBtn.observe(viewLifecycleOwner) {
            if(it){
                //binding.imgBtnStart.tag="ON"
                startBtn.setImageResource(R.drawable.state_start_img)
                startBtn.setBackgroundResource(R.drawable.round_start_btn)
            }
            else{
                //binding.imgBtnStart.tag="OFF"
                startBtn.setImageResource(R.drawable.state_stop_img)
                startBtn.setBackgroundResource(R.drawable.round_stop_btn)
            }
        }
    }


    private fun connectToGattServer(){
        bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
        bluetoothService?.let { bluetooth ->
            if (!bluetooth.initializeBlAdaptor()) {
                println("$TAG: Unable to initialize Bluetooth")
            }
            else{
                // call functions on service to check connection and connect to devices
                if(BluetoothDeviceListHelper.selectedDeviceInfo != null){
                    bluetooth.connect(BluetoothDeviceListHelper.selectedDeviceInfo!!.addr)
                }
                else {
                    Toast.makeText(activity,
                        "No Device is selected...",
                        Toast.LENGTH_SHORT).show();
                    println("$TAG: No device is selected...")
                }

            }
        }
    }


    private fun disconnectGattServer() {
        bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
        bluetoothService?.let { bluetooth ->
            if (!bluetooth.initializeBlAdaptor()) {
                println("$TAG: Unable to initialize Bluetooth")
            }
            else{
                bluetooth.disConnect()
            }
        }
    }


    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BLeDeviceServices.ACTION_GATT_CONNECTED -> {
                    Toast.makeText(activity,
                        "Device is connected and ready for data transfer...",
                        Toast.LENGTH_SHORT).show();

                    updateStartBtn()
                }
                BLeDeviceServices.ACTION_GATT_DISCONNECTED -> {
                    Toast.makeText(activity,
                        "Device is disconnected...",
                        Toast.LENGTH_SHORT).show();

                    updateStartBtn()
                }
                BLeDeviceServices.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    //bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
                    BluetoothDeviceListHelper.updateCharacteristic()
                    setNotifyCharacteristic()
                }
                BLeDeviceServices.ACTION_DATA_AVAILABLE->{

                    println("$TAG: Data is available ${intent.getStringExtra(Constants.DATA_KEY).toString()}")

                }
            }
        }
    }




    private fun setNotifyCharacteristic(){
        val uuid = Constants.BLE_DEVICE_CHARACTERISTIC_UUID
        val chara:BluetoothGattCharacteristic? = BluetoothDeviceListHelper.getCharacteristicsFormUUID(uuid)
        if(chara != null){
            bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
            bluetoothService?.setCharacteristicNotification(chara,true)
        }
    }

    private fun sendData(data:String){
        val uuid = Constants.BLE_DEVICE_CHARACTERISTIC_UUID
        val chara:BluetoothGattCharacteristic? = BluetoothDeviceListHelper.getCharacteristicsFormUUID(uuid)
        if(chara != null) {
            bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
            bluetoothService?.writeCharacteristic(chara, data.toByteArray())
        }
    }


    private fun readCharacteristics(){
        val uuid = Constants.BLE_DEVICE_CHARACTERISTIC_UUID
        val chara:BluetoothGattCharacteristic? = BluetoothDeviceListHelper.getCharacteristicsFormUUID(uuid)
        if(chara != null){
            bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
            bluetoothService?.readCharacteristic(chara)
        }
    }


    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BLeDeviceServices.ACTION_GATT_CONNECTED)
            addAction(BLeDeviceServices.ACTION_GATT_DISCONNECTED)
            addAction(BLeDeviceServices.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BLeDeviceServices.ACTION_DATA_AVAILABLE)
        }
    }

    override fun onResume() {
        super.onResume()
        println("$TAG: Resume started...")
        initRegisterReciever()
    }

    override fun onPause() {
        super.onPause()
        println("$TAG: On pause happened....")
        mContext?.unregisterReceiver(gattUpdateReceiver)
    }




    private fun updateDeviceInfo() {
        val device = BluetoothDeviceListHelper.selectedDeviceInfo
        if(device != null){
            if(device.isSelected){
                bleViewModel.deviceAddress.value = "Device: ${device.addr}"
                bleViewModel.deviceName.value = "Addr: ${device.name}"
            }
        }else{
            bleViewModel.deviceAddress.value = "Addr: YY"
            bleViewModel.deviceName.value = "Device: XX"
        }
    }

    private fun updateStartBtn() {
        if(BluetoothDeviceListHelper.isBleDeviceConnected()){
            bleViewModel.startBtn.value= true
        }
        else{
            bleViewModel.startBtn.value = false
        }
    }


    override fun onClick(v: View?) {
        if(v?.id == binding.bleStartBtn.id){
            if(BluetoothDeviceListHelper.isBleDeviceConnected()){
                showDialogForStopDataTransfer("Device Connection","Do you want to close it ...?")
            }else{
                connectToGattServer()
            }

        }
        else if(v?.id == binding.actionBleFrontBtn.id){
            
            sendData("f:+1")
        }
        else if(v?.id == binding.actionBleBackBtn.id){
            sendData("b:+1")
        }
        else if(v?.id == binding.actionBleLeftBtn.id){
            sendData("l:+1")
        }
        else if(v?.id == binding.actionBleRightBtn.id){
            sendData("r:+1")
        }

        else if(v?.id == binding.bleEditUuid.id){
            openBottomSheet()
        }
    }


    private fun openBottomSheet(){
        val modal = BleUpdateUuid(mContext!!)
        activity?.supportFragmentManager.let { modal.show(it!!, BleUpdateUuid.TAG) }
    }

    private fun initBtnOnClick(){
        val startBtn : ImageButton = binding.bleStartBtn
        startBtn.setOnClickListener(this)

        val frontBtn: FloatingActionButton = binding.actionBleFrontBtn
        frontBtn.setOnClickListener(this)

        val backBtn: FloatingActionButton = binding.actionBleBackBtn
        backBtn.setOnClickListener(this)

        val leftBtn: FloatingActionButton = binding.actionBleLeftBtn
        leftBtn.setOnClickListener(this)

        val rightBtn: FloatingActionButton = binding.actionBleRightBtn
        rightBtn.setOnClickListener(this)

        val editBtn:ImageButton = binding.bleEditUuid
        editBtn.setOnClickListener(this)


    }

         private fun showDialogForStopDataTransfer(title:String, message:String){
         if(mContext != null){
             val builder: AlertDialog.Builder = AlertDialog.Builder(mContext!!)
             builder.setTitle(title)
                 .setMessage(message)
                 .setPositiveButton("disconnect") { _, _ ->
                     disconnectGattServer()
                     updateStartBtn()
                 }.setNegativeButton(android.R.string.cancel){_,_ ->

                 }
             builder.create().show()
         }
         else{
             println("${TAG}: mContext is null....")
         }


     }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bluetoothService = BluetoothDeviceListHelper.getBleServiceClass()
        bluetoothService?.disConnect()
    }

}













/*

 private var mGattCharacteristics:MutableList<BluetoothGattCharacteristic> = mutableListOf()
   private var isDeviceConnected:Boolean = false
 displayGattServices(bluetoothService?.getSupportedGattServices())

private fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
            if (gattServices == null) return
            var uuid: String?
            //val unknownServiceString: String = resources.getString(R.string.unknown_service)
            //val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
            val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
            val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
                mutableListOf()
            mGattCharacteristics = mutableListOf()

            // Loops through available GATT Services.
            gattServices.forEach { gattService ->
                val currentServiceData = HashMap<String, String>()
                uuid = gattService?.uuid.toString()

                println("UUID of service: $uuid")

                currentServiceData["NAME"] = Constants.getServiceAttr()["name"]!!
                currentServiceData["UUID"] = uuid!!
                gattServiceData += currentServiceData

                val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
                val gattCharacteristics = gattService?.characteristics
                val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

                // Loops through available Characteristics.
                gattCharacteristics?.forEach { gattCharacteristic ->

                    charas += gattCharacteristic
                    val currentCharaData: HashMap<String, String> = hashMapOf()
                    uuid = gattCharacteristic.uuid.toString()

                    println("UUID of Characteristic: $uuid ")
                    println("WriteType of chara: ${gattCharacteristic.writeType}\n")
                    println("Descriptors: ${gattCharacteristic.getDescriptor(gattCharacteristic.uuid)}")
                    println("Descriptors list: \n${gattCharacteristic.descriptors.forEach { println("Descriptor uuid: ${it.uuid}\n") }}")

                    currentCharaData["NAME"] = Constants.getServiceAttr()["charas"]!!
                    currentCharaData["UUID"] = uuid!!
                    gattCharacteristicGroupData += currentCharaData
                }
                mGattCharacteristics += charas
                gattCharacteristicData += gattCharacteristicGroupData

            }


        //println("$TAG: Overall characteristics found: $mGattCharacteristics")

    }

*/