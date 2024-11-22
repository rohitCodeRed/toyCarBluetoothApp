package com.example.toycarbluetoothapp.ui.classic

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.toycarbluetoothapp.Constants
import com.example.toycarbluetoothapp.R
import com.example.toycarbluetoothapp.bluetooth.BlClassicConnectAsClientSocketThread
import com.example.toycarbluetoothapp.bluetooth.BluetoothDataTransferThread
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceListHelper
import com.example.toycarbluetoothapp.databinding.FragmentClassicBinding
import com.example.toycarbluetoothapp.ui.bottomsheet.BleUpdateUuid
import com.example.toycarbluetoothapp.ui.bottomsheet.ClassicUpdateUuid
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID


class ClassicFragment : Fragment(), OnClickListener{

    val TAG:String="ClassicFragment"
    private var _binding: FragmentClassicBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var classicViewModel: ClassicViewModel
    lateinit var  dataTransferClass: BluetoothDataTransferThread
    private var mContext:Context? = null
    private var  socketCreationClass: BlClassicConnectAsClientSocketThread? = null
    private  var bluetoothAdapter: BluetoothAdapter? = null
    private var isConnecting:Boolean = false


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
        classicViewModel = ViewModelProvider(this).get(ClassicViewModel::class.java)

        _binding = FragmentClassicBinding.inflate(inflater, container, false)
        val root: View = binding.root


        initHandler()
        initReference()

        initDataViewModel()

        initBtnOnClick()

        updateStartBtn()

        updateDeviceInfo()

        return root
    }


     private fun initHandler() {
         BluetoothDeviceListHelper.setActivityHandler(pHandler)
     }

    private fun initReference(){
        dataTransferClass = BluetoothDataTransferThread()
        socketCreationClass = BluetoothDeviceListHelper.getSocketCreateClass()
        bluetoothAdapter = BluetoothDeviceListHelper.getAdaptor()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        stopConnection()
    }


    private fun initDataViewModel(){

        val deviceName: TextView = binding.textDeviceName;
        classicViewModel.deviceName.observe(viewLifecycleOwner) {
            deviceName.text = it
        }

        val deviceAddr: TextView = binding.textDeviceAddr;
        classicViewModel.deviceAddress.observe(viewLifecycleOwner) {
            deviceAddr.text = it
        }

        val recieveSpeed: TextView = binding.textRpmValue;
        classicViewModel.recieveSpeed.observe(viewLifecycleOwner) {
            recieveSpeed.text = it
        }

        val obstacleBtn: RadioButton = binding.rBtnObstacle;
        classicViewModel.r_btn_obstacle.observe(viewLifecycleOwner) {
            obstacleBtn.isChecked = it
        }

        val messageView: TextView = binding.textUnit;
        classicViewModel.messageUnit.observe(viewLifecycleOwner) {
            messageView.text = it
        }

        val startBtn: ImageButton = binding.imgBtnStart;
        classicViewModel.startBtn.observe(viewLifecycleOwner) {
            if(it){
                startBtn.setImageResource(R.drawable.state_start_img)
                startBtn.setBackgroundResource(R.drawable.round_start_btn)
            }
            else{
                startBtn.setImageResource(R.drawable.state_stop_img)
                startBtn.setBackgroundResource(R.drawable.round_stop_btn)
            }
        }

    }


    private var pHandler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.MESSAGE_READ ->{
                updateDynamicTextView(it.obj.toString())
            }
            Constants.MESSAGE_WRITE ->{
                println("$TAG: Written happened, ${it.obj}")
            }
//            Constants.MESSAGE_TOAST ->{
//
//                if (null != activity) {
//                    Toast.makeText(activity, it.getData().getString(Constants.MSG_KEY),
//                        Toast.LENGTH_SHORT).show();
//                }
//            }
            Constants.CLIENT_SOCKET ->{
                if(it.obj == Constants.CONNECT){

                    startDataTransfer()

                }
                else if(it.obj == Constants.DIS_CONNECT){
                    updateStartBtn()
                    Toast.makeText(activity,
                        "Classic Device not able to create socket",
                        Toast.LENGTH_SHORT).show();
                    isConnecting = false
                }
            }
            Constants.CONNECTION_STOP ->{
                updateStartBtn()
                Toast.makeText(activity,
                    "Device is Disconnected...",
                    Toast.LENGTH_SHORT).show();
                isConnecting = false

            }
            Constants.CONNECT->{
                updateStartBtn()
                Toast.makeText(activity,
                    "Device is Ready for Communication",
                    Toast.LENGTH_SHORT).show();
                isConnecting = false
                sendData("s");

            }
        }
        return@Handler true
    }

     private fun updateDeviceInfo() {
         val device = BluetoothDeviceListHelper.selectedDeviceInfo
         if(device != null){
             if(device.isSelected){
                 classicViewModel.deviceAddress.value = "Device: ${device.addr}"
                 classicViewModel.deviceName.value = "Addr: ${device.name}"
             }
         }else{
             classicViewModel.deviceAddress.value = "Addr: YY"
             classicViewModel.deviceName.value = "Device: XX"
         }

     }


     private fun updateDynamicTextView(m:String) {
         println("$TAG: Incomming string $m")
         val formatedText = m.replace(" ","").replace("\n","").replace("\r","")


         //Split string by :
         val splitColon = formatedText.split(":")
         println("$TAG: Split by colon: $splitColon")

         if(splitColon.count() >= 4){
             if(splitColon[0] != "" && splitColon[1] != ""){
                 if(splitColon[0].toIntOrNull() != null && splitColon[1].toIntOrNull() != null) {
                     classicViewModel.recieveSpeed.value = "${splitColon[0]} : ${splitColon[1]}"
                 }
             }

             if(splitColon[3].contains("r")){
                 classicViewModel.messageUnit.value = "Direction is Reversed...!!"
             }else {
                 classicViewModel.messageUnit.value = "Direction is Forward...!!"
             }


             if(splitColon[2].contains("t")){
                 classicViewModel.r_btn_obstacle.value = true
                 classicViewModel.messageUnit.value = "Obstacle Ahead...!!"
             }else {
                 classicViewModel.r_btn_obstacle.value = false
             }
         }
     }


     private fun initBtnOnClick(){
        val startBtn : ImageButton = binding.imgBtnStart
        startBtn.setOnClickListener(this)

        val accBtn:FloatingActionButton = binding.fBtnAcc
        accBtn.setOnClickListener(this)

        val decAccBtn:FloatingActionButton = binding.fBtnDeAcc
        decAccBtn.setOnClickListener(this)

        val leftBtn:FloatingActionButton = binding.fBtnLeft
        leftBtn.setOnClickListener(this)

        val rightBtn:FloatingActionButton = binding.fBtnRight
        rightBtn.setOnClickListener(this)

         val brakeBtn:FloatingActionButton = binding.actionBreakBtn
         brakeBtn.setOnClickListener(this);

         val editBtn:ImageButton = binding.editClassicUuid
         editBtn.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
            if(v?.id == binding.imgBtnStart.id){

                if(!isConnecting){
                    if(dataTransferClass.getThreadStatus() == true){
                        //stopDataTransfer()
                        showDialogForStopDataTransfer("Device Connection","Do you want to close it ...?")
                    }else{
                        startConnection()
                    }
                }
                else{
                    Toast.makeText(activity,
                        "Device is connecting....",
                        Toast.LENGTH_SHORT).show();
                }


            }
            else if(v?.id == binding.fBtnAcc.id){
                sendData("a")
            }
            else if(v?.id == binding.fBtnDeAcc.id){
                sendData("d")
            }
            else if(v?.id == binding.fBtnLeft.id){
                sendData("l")
            }
            else if(v?.id == binding.fBtnRight.id){
                sendData("r")
            }else if(v?.id == binding.actionBreakBtn.id){
                sendData("b")
            }
            else if(v?.id == binding.editClassicUuid.id){
                openBottomSheet()
            }
    }
    private fun openBottomSheet(){
        val modal = ClassicUpdateUuid(mContext!!)
        activity?.supportFragmentManager.let { modal.show(it!!, ClassicUpdateUuid.TAG) }
    }


     fun sendData(s:String){
         dataTransferClass.sendCommand(s)
     }


    private fun startDataTransfer(){

        val socket = BluetoothDeviceListHelper.getClientSocket()
        if(socket != null){
            dataTransferClass.setThread(socket,pHandler)
            dataTransferClass.startThread()
        }


    }


     private fun updateStartBtn(){
         if(dataTransferClass.getThreadStatus() == true){
             classicViewModel.startBtn.value= true
         }else{
             classicViewModel.startBtn.value = false
         }
     }

     private fun showDialogForStopDataTransfer(title:String, message:String){

             val builder: AlertDialog.Builder = AlertDialog.Builder(mContext!!)
             builder.setTitle(title)
                 .setMessage(message)
                 .setPositiveButton("disconnect") { _, _ ->
                     stopConnection()
                 }.setNegativeButton(android.R.string.cancel){_,_ ->

                 }

             builder.create().show()

     }

    private fun startConnection(){
        if(BluetoothDeviceListHelper.getBluetoothStatus() && BluetoothDeviceListHelper.getPermissionStatus()){
            var uuid: UUID?
            try {
                uuid = UUID.fromString(Constants.CLIENT_UUID)
            }catch (e:Exception){
                uuid = null
                Toast.makeText(
                    activity,
                    "Error occurred while socket connect: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show();
                return
            }

            val device = BluetoothDeviceListHelper.selectedDeviceInfo

            if(device?.device != null){
                isConnecting = true
                Toast.makeText(activity,
                    "Device is connecting....",
                    Toast.LENGTH_SHORT).show();


                socketCreationClass!!.setThread(device.device!!,uuid)
                socketCreationClass!!.startThread()

                return
            }

            Toast.makeText(activity,
                "Please select device..",
                Toast.LENGTH_SHORT).show();

            return
        }
        Toast.makeText(activity,
            "Please set permission and turn on the bluetooth...",
            Toast.LENGTH_SHORT).show();
    }

    private fun stopConnection(){
        sendData("b");

        dataTransferClass.cancelThread()
        socketCreationClass?.cancelThread()
    }



 }



