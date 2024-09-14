package com.example.toycarbluetoothapp.ui.ble

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
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
import com.example.toycarbluetoothapp.bluetooth.BleDeviceListServices
import com.example.toycarbluetoothapp.databinding.FragmentBleBinding
import android.view.View.OnClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class BleFragment : Fragment(), OnClickListener{
    private val TAG = "BLeFragmentActivity"
    private var _binding: FragmentBleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mContext: Context? =null
    private lateinit var bleViewModel: BleViewModel

    private var bluetoothService : BLeDeviceServices? = null

    private var mGattCharacteristics:MutableList<BluetoothGattCharacteristic> = mutableListOf()

    private var isDeviceConnected:Boolean = false




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

        val gattServiceIntent = Intent(mContext, BLeDeviceServices::class.java)
        mContext?.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)


        initDataViewModel()

        initHandler()
        initBtnOnClick()

        initRegisterReciever()

        updateDeviceInfo()
        updateStartBtn()

        return root
    }

    private fun initRegisterReciever() {
        registerReceiver(mContext!!,gattUpdateReceiver, makeGattUpdateIntentFilter()!!,
            ContextCompat.RECEIVER_EXPORTED
        )
    }


    private fun initHandler() {
         BleDeviceListServices.setActivityHandler(pHandler)
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

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            println("$TAG: Binding happen successfully....\n")
            bluetoothService = (service as BLeDeviceServices.LocalBinder).getService()
            BleDeviceListServices.setBleServiceClass(bluetoothService)

//            bluetoothService?.let { bluetooth ->
//                if (!bluetooth.initializeBlAdaptor()) {
//                    println("$TAG: Unable to initialize Bluetooth")
//                }
//                else{
//                    // call functions on service to check connection and connect to devices
//                    if(BleDeviceListServices.selectedDeviceInfo != null){
//                        bluetooth.connect(BleDeviceListServices.selectedDeviceInfo!!.addr)
//
//                    }
//                    else {
//                        println("$TAG: No device is selected...")
//                    }
//
//                }
//            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            println("$TAG: Binding disconnected.....")
            bluetoothService = null
        }
    }

    private fun connectToGattServer(){
        bluetoothService?.let { bluetooth ->
            if (!bluetooth.initializeBlAdaptor()) {
                println("$TAG: Unable to initialize Bluetooth")
            }
            else{
                // call functions on service to check connection and connect to devices
                if(BleDeviceListServices.selectedDeviceInfo != null){
                    bluetooth.connect(BleDeviceListServices.selectedDeviceInfo!!.addr)

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
        bluetoothService?.let { bluetooth ->
            if (!bluetooth.initializeBlAdaptor()) {
                println("$TAG: Unable to initialize Bluetooth")
            }
            else{
                // call functions on service to check connection and connect to devices
                bluetooth.disConnect()
            }
        }
    }


    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BLeDeviceServices.ACTION_GATT_CONNECTED -> {
                    BleDeviceListServices.setBleDeviceConnectStatus(true)
                    isDeviceConnected = true
                    Toast.makeText(activity,
                        "Device is connected and ready for data transfer...",
                        Toast.LENGTH_SHORT).show();
                    updateStartBtn()
                    //updateDeviceInfo()

//                    updateConnectionState(R.string.connected)
                }
                BLeDeviceServices.ACTION_GATT_DISCONNECTED -> {
                    BleDeviceListServices.setBleDeviceConnectStatus(false)
                    isDeviceConnected = false
                    Toast.makeText(activity,
                        "Device is disconnected...",
                        Toast.LENGTH_SHORT).show();
                    updateStartBtn()
                    //updateDeviceInfo()
//                    updateConnectionState(R.string.disconnected)
                }
                BLeDeviceServices.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(bluetoothService?.getSupportedGattServices())
                }
                BLeDeviceServices.ACTION_DATA_AVAILABLE->{
                    println("$TAG: Data is available ${intent.getByteArrayExtra("CHARAS_DATA")}")
                }
            }
        }
    }


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
                    currentCharaData["NAME"] = Constants.getServiceAttr()["charas"]!!
                    currentCharaData["UUID"] = uuid!!
                    gattCharacteristicGroupData += currentCharaData
                }
                mGattCharacteristics += charas
                gattCharacteristicData += gattCharacteristicGroupData

            }


        println("$TAG: Overall characteristics found: $mGattCharacteristics")

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
        println("$TAG: Resume started")
        initRegisterReciever()
        //connectToGattServer()

    }

    override fun onPause() {
        super.onPause()
        println("$TAG: On pause happened....")
        mContext?.unregisterReceiver(gattUpdateReceiver)
    }



    private var pHandler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.MESSAGE_READ ->{

            }
            Constants.MESSAGE_WRITE ->{
                println("Written happened, ${it.obj}")
            }
            Constants.MESSAGE_DEVICE_NAME->{

            }
            Constants.MESSAGE_TOAST ->{

                if (null != activity) {
                    Toast.makeText(activity, it.getData().getString(Constants.MSG_KEY),
                        Toast.LENGTH_SHORT).show();
                }
            }
            Constants.CONNECTION_STOP ->{
                updateStartBtn()
                updateDeviceInfo()
                Toast.makeText(activity,
                    "Device is Disconnected",
                    Toast.LENGTH_SHORT).show();
            }
            Constants.CONNECTION_ERROR ->{
                updateStartBtn()
                println("Connection error occurred..${it.obj}")
            }
            Constants.CONNECT->{
                updateStartBtn()
                updateDeviceInfo()
                Toast.makeText(activity,
                    "Device is Ready for Communication",
                    Toast.LENGTH_SHORT).show();
            }
        }
        return@Handler true
    }

    private fun updateDeviceInfo() {
        val device = BleDeviceListServices.selectedDeviceInfo
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
        if(isDeviceConnected){
            bleViewModel.startBtn.value= true
        }
        else{
            bleViewModel.startBtn.value = false
        }
    }


    override fun onClick(v: View?) {
        if(v?.id == binding.bleStartBtn.id){
            if(isDeviceConnected){
                showDialogForStopDataTransfer("Device Connection","Do you want to close it ...?")
            }else{
                connectToGattServer()
            }

        }
//        else if(v?.id == binding.fBtnAcc.id){
//            sendData("+1")
//        }
//        else if(v?.id == binding.fBtnDeAcc.id){
//            sendData("-1")
//        }
//        else if(v?.id == binding.fBtnLeft.id){
//            sendData("-1")
//        }
//        else if(v?.id == binding.fBtnRight.id){
//            sendData("+1")
//        }
    }

    private fun initBtnOnClick(){
        val startBtn : ImageButton = binding.bleStartBtn
        startBtn.setOnClickListener(this)

//        val accBtn: FloatingActionButton = binding.fBtnAcc
//        accBtn.setOnClickListener(this)
//
//        val decAccBtn: FloatingActionButton = binding.fBtnDeAcc
//        decAccBtn.setOnClickListener(this)
//
//        val leftBtn: FloatingActionButton = binding.fBtnLeft
//        leftBtn.setOnClickListener(this)
//
//        val rightBtn: FloatingActionButton = binding.fBtnRight
//        rightBtn.setOnClickListener(this)

    }

         private fun showDialogForStopDataTransfer(title:String, message:String){
         if(mContext != null){
             val builder: AlertDialog.Builder = AlertDialog.Builder(mContext!!)
             builder.setTitle(title)
                 .setMessage(message)
                 .setPositiveButton(android.R.string.ok) { _, _ ->
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
    }

}