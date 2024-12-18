package com.example.toycarbluetoothapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.ui.AppBarConfiguration
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceInfo
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceListHelper
import com.example.toycarbluetoothapp.databinding.ActivityBluetoothDeviceListBinding

class BluetoothDeviceListActivity : AppCompatActivity(), OnClickListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityBluetoothDeviceListBinding

    private val TAG = "BluetoothDeviceListActivity"
    //private lateinit var binding:
    private  var bluetoothAdapter: BluetoothAdapter? = null
    private  var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false

    private lateinit var pLinearLayout: LinearLayout

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = Constants.BLE_SCAN_PERIOD

    private val timerCallbackHandler = Handler(Looper.myLooper()!!)

    private val eventHandler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.DEVICE_FOUND_UPDATE ->{
                if(it.obj == Constants.ONE_DEVICE){
                    println("One device added\n")
                    //updateLayoutWithRealDataOneByOne()
                }else if(it.obj == Constants.WHOLE_DEVICE_LIST){
                    println("whole device updated\n")
                    //updateLayoutWithRealData()
                }
            }
            Constants.DISCOVERY_MODE ->{
                if(it.obj == Constants.START){

                }
                else{

                }
            }
       }

        return@Handler true
    }


    private val bluetoothUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND->{
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE,BluetoothDevice::class.java)
                    if(BluetoothDeviceListHelper.addDevice(device)){
                        addView(device!!)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED->{
                    scanning = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    scanning = false
                    Toast.makeText(this@BluetoothDeviceListActivity, "Scanning of device ended, click to restart...", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBluetoothDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.deviceListToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pLinearLayout = findViewById(R.id.device_list_layout)


        initHandler()
        initBleScanner()
        scanDevice()

    }



    private fun initHandler() {
        BluetoothDeviceListHelper.setActivityHandler(eventHandler)
    }

    private fun initBleScanner(){
        bluetoothAdapter = BluetoothDeviceListHelper.getAdaptor()
        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_bluetooth, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_startDisc ->{
                scanDevice()
            }
//            android.R.id.home->{
//                finish()
//            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun scanDevice() {
        if(BluetoothDeviceListHelper.getBluetoothStatus() && BluetoothDeviceListHelper.getPermissionStatus()){
            clearView()
            clearDeviceList()

            val pairedDevices:MutableList<BluetoothDeviceInfo>  = BluetoothDeviceListHelper.scanBluetoothPairedDevice()
            pairedDevices.forEach {
                addView(it.device!!)
            }

            if (!scanning) { // Stops scanning after a pre-defined scan period.
                timerCallbackHandler.postDelayed({
                    scanning = false
                    bluetoothLeScanner?.stopScan(leScanCallback)

                    startDeviceDiscovery()

                }, SCAN_PERIOD)
                scanning = true
                Toast.makeText(this, "Scanning of device started...", Toast.LENGTH_SHORT).show();
                bluetoothLeScanner?.startScan(leScanCallback)
            }
            else {
                Toast.makeText(this, "Scanning of device going on...", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please turn on the bluetooth and get permission set...", Toast.LENGTH_SHORT).show();
        }

    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)


            try {
                if(BluetoothDeviceListHelper.addDevice(result.device)){
                    addView(result.device)
                }
                //BleDeviceListServices.notifyDataSetChanged()
            }catch(e:Exception){
                Toast.makeText(this@BluetoothDeviceListActivity, "Not able to add device..${e.message}", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private fun startDeviceDiscovery() {

        if(BluetoothDeviceListHelper.getBluetoothStatus() && BluetoothDeviceListHelper.getPermissionStatus()){
            if(bluetoothAdapter != null){
                if(bluetoothAdapter!!.isDiscovering){
                    bluetoothAdapter!!.cancelDiscovery()
                }
                bluetoothAdapter!!.startDiscovery()

                return
            }
        }

        Toast.makeText(this, "Please turn on the bluetooth and get permission set...", Toast.LENGTH_SHORT).show();

    }


    private fun updateCardView(devInfo: BluetoothDeviceInfo): CardView {
        val inflatedView: View = layoutInflater.inflate(R.layout.card_device,null)
        val pCardView: CardView = inflatedView.findViewById(R.id.device_card_view)

        if(devInfo.name != null){
            pCardView.findViewById<TextView>(R.id.device_name).text = devInfo.name
        }else{
            pCardView.findViewById<TextView>(R.id.device_name).text =buildString { append("No name") }
        }


        pCardView.findViewById<TextView>(R.id.mac_addr).text = devInfo.addr

        //pCardView.findViewById<Button>(R.id.conn_btn).setOnClickListener(this)

        if(devInfo.isSelected){
            pCardView.findViewById<Button>(R.id.conn_btn).text = buildString { append("deSelect") }
            pCardView.tag = "deSelect"
        }
        else{
            pCardView.findViewById<Button>(R.id.conn_btn).text = buildString { append("select") }
            pCardView.tag = "select"
        }

        pCardView.setOnClickListener(this)

        return pCardView
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.device_card_view){
            val name = v.findViewById<TextView>(R.id.device_name).text
            val mac_addr = v.findViewById<TextView>(R.id.mac_addr).text


            val device = BluetoothDeviceListHelper.getDeviceByAddress(mac_addr.toString())
            if(device != null){
                if(device.isSelected){
                    showDialogForSocketDisConnect("Ble Device","Do you want to de select the device : ${name}..?",device)
                }else{
                    //selectDevice(device,null)
                    dialogForTypeOfDevice(device)
                }
            }else{
                Toast.makeText(this, "Device is dummy...", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private fun selectDevice(d: BluetoothDeviceInfo,type:String?) {
            BluetoothDeviceListHelper.selectDevice(d,type)
            updateDeviceListView()
            Toast.makeText(this, "Device: ${d.name} is selected...", Toast.LENGTH_SHORT).show()
    }

    private fun updateDeviceListView() {
        clearView()
        val data = BluetoothDeviceListHelper.getDeviceList()
        data.forEach {
            pLinearLayout.addView(updateCardView(it))
        }
    }


    private fun deSelectDevice(d: BluetoothDeviceInfo) {
        BluetoothDeviceListHelper.deSelectDevice(d)
        updateDeviceListView()
    }


    private fun addView(d: BluetoothDevice){
        pLinearLayout.addView(updateCardView(BluetoothDeviceInfo(d.name,d.address,d,false)))
    }


    private fun clearView(){
        pLinearLayout.removeAllViews()
    }

    private fun clearDeviceList(){
        BluetoothDeviceListHelper.clearAllAvailableDevices()
    }

    private fun showDialogForSocketDisConnect(title:String, message:String,d: BluetoothDeviceInfo){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("deselect") { _, _ ->
                deSelectDevice(d)
            }.setNegativeButton(android.R.string.cancel){_,_ ->

            }

        builder.create().show()

    }

    private fun dialogForTypeOfDevice(d: BluetoothDeviceInfo?){
        var selectedType:String? =""
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Select Device as")
            .setPositiveButton("ok") { dialog, which ->

                selectDevice(d!!,selectedType)
            }
            .setNegativeButton("cancel") { dialog, which ->
                // Do something else.
            }
            .setSingleChoiceItems(
                arrayOf("Classic Device", "Ble Device"),-1
            ) { dialog, which ->
                when(which){
                    0->{
                        selectedType = "Classic"
                    }
                    1->{
                        selectedType= "Ble"
                    }
                }
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }


    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        println("$TAG: Resume started")
        initRegisterReceiver()
    }


    override fun onPause() {
        super.onPause()
        println("$TAG: On pause happened....")
        try {
            deRegisterReceiver()

        }catch(e:Exception){
            println("$TAG: Error occur while deRegisterReceiver")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRegisterReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bluetoothUpdateReceiver,makeBlIntentFilter(), RECEIVER_EXPORTED)
        }
    }

    private fun deRegisterReceiver() {
        unregisterReceiver(bluetoothUpdateReceiver)
    }

    private fun makeBlIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
    }
}