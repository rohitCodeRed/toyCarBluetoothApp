package com.example.toycarbluetoothapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.example.toycarbluetoothapp.bluetooth.BleDeviceInfo
import com.example.toycarbluetoothapp.bluetooth.BleDeviceListServices
import com.example.toycarbluetoothapp.bluetooth.BlClassicDeviceServices
import com.google.android.material.appbar.MaterialToolbar


class BleDeviceListActivity : AppCompatActivity(), OnClickListener {
    private val TAG = "BleDeviceListActivity"
    //private lateinit var binding:
    private  var bluetoothAdapter: BluetoothAdapter? = null
    private  var bluetoothLeScanner:BluetoothLeScanner? = null
    private var scanning = false

    private lateinit var pLinearLayout: LinearLayout

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = Constants.BLE_SCAN_PERIOD

    private val callBackHandler = Handler(Looper.myLooper()!!)
    private val pHandler = Handler(Looper.myLooper()!!){
//        when(it.what){
//            Constants.DEVICE_FOUND_UPDATE ->{
//                if(it.obj == Constants.ONE_DEVICE){
//                    println("One device added\n")
//                    updateLayoutWithRealDataOneByOne()
//                }else if(it.obj == Constants.WHOLE_DEVICE_LIST){
//                    println("whole device updated\n")
//
//                }
//            }
//        }

        return@Handler true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device)

        val toolBar = findViewById<MaterialToolbar>(R.id.ble_device_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pLinearLayout = findViewById(R.id.ble_device_linear_layout)


        initHandler()
        initBleScanner()


        scanLeDevice()



    }

    private fun initHandler() {
        BleDeviceListServices.setActivityHandler(pHandler)
    }

    private fun initBleScanner(){
        bluetoothAdapter = BleDeviceListServices.getAdaptor()
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
                scanLeDevice()
            }
//            android.R.id.home->{
//                finish()
//            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun scanLeDevice() {

        if(BleDeviceListServices.getBluetoothStatus() && BleDeviceListServices.getPermissionStatus()){
            clearView()
            clearDeviceList()
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                callBackHandler.postDelayed({
                    scanning = false
                    bluetoothLeScanner?.stopScan(leScanCallback)
                    Toast.makeText(this, "Scanning of device ended, click to restart...", Toast.LENGTH_SHORT).show();
                }, SCAN_PERIOD)
                scanning = true
                Toast.makeText(this, "Scanning of device started...", Toast.LENGTH_SHORT).show();
                bluetoothLeScanner?.startScan(leScanCallback)
            }
            else {

//                scanning = false
//                bluetoothLeScanner?.stopScan(leScanCallback)
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
                if(BleDeviceListServices.addDevice(result.device)){
                    addView(result.device)
                }
                //BleDeviceListServices.notifyDataSetChanged()
            }catch(e:Exception){
                Toast.makeText(this@BleDeviceListActivity, "Not able to add device..${e.message}", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private fun updateCardView(devInfo: BleDeviceInfo): CardView {
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

            val device = BleDeviceListServices.getDeviceByAddress(mac_addr.toString())
            if(device != null){
                if(device.isSelected){
                    //deSelectDevice(device)
                    showDialogForSocketDisConnect("Ble Device","Do you want to de select the device : ${name}..?",device)
                }else{
                    selectDevice(device)
                }
            }else{
                Toast.makeText(this, "Device is dummy...", Toast.LENGTH_SHORT).show();
            }

            //TODOD...

        }
    }

    private fun selectDevice(d:BleDeviceInfo) {
        BleDeviceListServices.selectDevice(d)
        updateDeviceListView()
        Toast.makeText(this, "Device: ${d.name} is selected...", Toast.LENGTH_SHORT).show()
    }

    private fun updateDeviceListView() {
        clearView()
        val data = BleDeviceListServices.getDeviceList()
        data.forEach {
            pLinearLayout.addView(updateCardView(it))
        }
    }

    private fun deSelectDevice(d:BleDeviceInfo) {
        BleDeviceListServices.deSelectDevice(d)
        updateDeviceListView()
    }


    private fun addView(d:BluetoothDevice){
        //if(!BleDeviceListServices.isDeviceAlreadyPresent(d.address)){
            pLinearLayout.addView(updateCardView(BleDeviceInfo(d.name,d.address,d,false)))
        //}

    }


    private fun clearView(){
        pLinearLayout.removeAllViews()
    }

    private fun clearDeviceList(){
        BleDeviceListServices.clearAllAvailableDevices()
    }

    private fun showDialogForSocketDisConnect(title:String, message:String,d:BleDeviceInfo){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deSelectDevice(d)
            }.setNegativeButton(android.R.string.cancel){_,_ ->

            }

        builder.create().show()

    }


}


