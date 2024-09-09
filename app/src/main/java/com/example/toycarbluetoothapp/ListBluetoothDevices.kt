package com.example.toycarbluetoothapp

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.example.toycarbluetoothapp.bluetooth.BluetoothConnectAsClientSocketThread
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceServices
import com.example.toycarbluetoothapp.bluetooth.Constants
import com.example.toycarbluetoothapp.bluetooth.SampleDeviceInfo
import com.example.toycarbluetoothapp.databinding.ActivityListBluetoothDevicesBinding
import java.util.UUID


class ListBluetoothDevices : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityListBluetoothDevicesBinding
    private lateinit var deviceData:MutableList<SampleDeviceInfo>
    private lateinit var pLinearLayout:LinearLayout
    private  var bluetoothAdapter: BluetoothAdapter? = null
    private var  socketCreationClass: BluetoothConnectAsClientSocketThread? = null

    private var pHandler: Handler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.DEVICE_FOUND_UPDATE ->{
                if(it.obj == Constants.ONE_DEVICE){
                    println("One device added\n")
                    updateLayoutWithRealDataOneByOne()
                }else if(it.obj == Constants.WHOLE_DEVICE_LIST){
                    println("whole device updated\n")
                    updateLayoutWithRealData()
                }
            }
            Constants.CLIENT_SOCKET ->{
                if(it.obj == Constants.CONNECT){
                    updateLayoutWithRealData()
                    Toast.makeText(
                        this,
                        "Device Socket is Connected",
                        Toast.LENGTH_SHORT
                    ).show();
                   // println("Socket created\n")
                }
                else if(it.obj == Constants.DIS_CONNECT){
                    updateLayoutWithRealData()
                    Toast.makeText(
                        this,
                        "Device Socket is Disconnected",
                        Toast.LENGTH_SHORT
                    ).show();
                   // println("Socket disconnected.....\n")
                }
            }
            Constants.CLIENT_SOCKET_ERROR -> {
                Toast.makeText(
                    this,
                    "Error occurred while connecting Device Socket: ${it.obj}",
                    Toast.LENGTH_SHORT
                ).show();
            }
            Constants.DISCOVERY_MODE ->{
            if(it.obj == Constants.START){
                Toast.makeText(
                    this,
                    "Scanning of devices started",
                    Toast.LENGTH_SHORT
                ).show();
                //println("Discovery started\n")
            }
//            else{
//                Toast.makeText(
//                    this,
//                    "Scanning of devices Ended",
//                    Toast.LENGTH_SHORT
//                ).show();
//                //println("Discovery ended\n")
//            }
            }
        }
        return@Handler false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBluetoothDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.deviceToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //deviceData = this.createSampleData()

        pLinearLayout = findViewById(R.id.device_linear_layout)


        initBlAndSocket()

        initHandler()

        startDeviceDiscovery()


    }


    private fun initHandler() {
        BluetoothDeviceServices.setActivityHandler(pHandler)
    }


    private fun initBlAndSocket(){
        socketCreationClass = BluetoothDeviceServices.getSocketCreateClass()
        bluetoothAdapter = BluetoothDeviceServices.getBluetoothAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_bluetooth, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_startDisc ->{
                startDeviceDiscovery()
            }
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(v: View?) {
        if(v?.id == R.id.device_card_view){
            val name = v.findViewById<TextView>(R.id.device_name).text
            val mac_addr = v.findViewById<TextView>(R.id.mac_addr).text
            println("card view click in condition ${mac_addr}")
            val device = BluetoothDeviceServices.getDeviceByAddress(mac_addr.toString())

                if(device!= null){
                   if(BluetoothDeviceServices.checkDeviceIsConnected(device) == true){
                       showDialogForSocketDisConnect("Device Connection", "Do you want to close it ...?")
                   }
                   else{
                       connectAsClient(device)
                   }

                }
        }

    }



    private fun startDeviceDiscovery() {

        if(!BluetoothDeviceServices.getPermission()){
            Toast.makeText(this, "Please, Enable the Permission from top right Action menu.", Toast.LENGTH_SHORT).show();
            return
        }

        if(!BluetoothDeviceServices.getBluetoothEnStatus()){
            Toast.makeText(this, "Please, Turn on the Bluetooth from top right Action menu.", Toast.LENGTH_SHORT).show();
            return
        }

        BluetoothDeviceServices.clearAllAvailableDevices()
        BluetoothDeviceServices.scanBluetoothPairedDevice()

     if(bluetoothAdapter != null){
         if(bluetoothAdapter!!.isDiscovering){
             bluetoothAdapter!!.cancelDiscovery()
         }
        bluetoothAdapter!!.startDiscovery()
     }

    }


    private fun showDialogForSocketDisConnect(title:String, message:String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                disconnectAsClient()
            }.setNegativeButton(android.R.string.cancel){_,_ ->

            }

        builder.create().show()

    }


    private fun createSampleData():MutableList<SampleDeviceInfo>{

        val tempList:MutableList<SampleDeviceInfo> = mutableListOf()
        tempList.add(SampleDeviceInfo("POCO f1 XX","56:gujucxb:8097bibecbbc:00"))
        tempList.add(SampleDeviceInfo("POCO f2 XX","56:gujucxb:8097bibecbbc:01",false))
        tempList.add(SampleDeviceInfo("POCO k4 XX","56:gujucxb:8097bibecbbc:02",false))
        tempList.add(SampleDeviceInfo("POCO s1 XX","56:gujucxb:8097bibecbbc:03",false))

        return tempList
    }


    private fun updateCardView(devInfo:SampleDeviceInfo): CardView{
        val inflatedView:View = layoutInflater.inflate(R.layout.card_device,null)
        val pCardView:CardView = inflatedView.findViewById(R.id.device_card_view)

        pCardView.findViewById<TextView>(R.id.device_name).text = devInfo.name
        pCardView.findViewById<TextView>(R.id.mac_addr).text = devInfo.mac_addr

        //pCardView.findViewById<Button>(R.id.conn_btn).setOnClickListener(this)

        if(devInfo.is_conn){
            pCardView.findViewById<Button>(R.id.conn_btn).text = buildString {
                append("Disconnect")
                pCardView.tag = "Disconnect"
         }
        }
        else{
            pCardView.findViewById<Button>(R.id.conn_btn).text = buildString {
                append("connect")
                pCardView.tag = "CONNECT"
            }
        }

        pCardView.setOnClickListener(this)

        return pCardView
    }

    private fun updateLinearLayout(){
        pLinearLayout.removeAllViews()
        pLinearLayout.addView(updateCardView(deviceData[0]))
        pLinearLayout.addView(updateCardView(deviceData[1]))
        pLinearLayout.addView(updateCardView(deviceData[2]))
        pLinearLayout.addView(updateCardView(deviceData[3]))
    }


    private fun updateLayoutWithRealData(){
        pLinearLayout.removeAllViews()
        val devData = BluetoothDeviceServices.getAllAvailableDevice()
        devData.forEach {
            pLinearLayout.addView(updateCardView(it))
        }
    }

    private fun updateLayoutWithRealDataOneByOne(){
        var devData = BluetoothDeviceServices.getAllAvailableDevice()
        if(pLinearLayout.childCount < devData.count()){
            devData = devData.subList(pLinearLayout.childCount,devData.count())
        }
        devData.forEach {
            pLinearLayout.addView(updateCardView(it))
        }
    }

    private fun connectAsClient(device: BluetoothDevice){
        var uuid:UUID?
        try {
            uuid = UUID.fromString(Constants.CLIENT_UUID)
        }catch (e:Exception){
            uuid = null
            Toast.makeText(
                this,
                "Error occurred while socket connect: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show();
            return
        }


        socketCreationClass!!.setThread(device,uuid)
        socketCreationClass!!.startThread()


    }


    private fun disconnectAsClient() {

        if (BluetoothDeviceServices.getClientSocket() != null) {
            socketCreationClass?.cancelThread()
        }
        BluetoothDeviceServices.disconnectClientSocket()

    }



}




