package com.example.toycarbluetoothapp.ui.classic

import android.app.Activity
import android.bluetooth.BluetoothSocket
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.toycarbluetoothapp.Constants
import com.example.toycarbluetoothapp.R
import com.example.toycarbluetoothapp.bluetooth.BluetoothDataTransferThread
import com.example.toycarbluetoothapp.bluetooth.BlClassicDeviceServices
import com.example.toycarbluetoothapp.databinding.FragmentClassicBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ClassicFragment : Fragment(), OnClickListener{

    private var _binding: FragmentClassicBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var classicViewModel: ClassicViewModel

    lateinit var  dataTransferClass: BluetoothDataTransferThread
    private var mContext:Context? =null


     override fun onAttach(context: Context) {
         super.onAttach(context)
         if (context is Activity) {
             mContext = context
         }

         //println("Context s${}")
         //mContext = context
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

        dataTransferClass = BluetoothDataTransferThread()

        initDataViewModel()

        initBtnOnClick()

        updateStartBtn()

        return root
    }


//     private fun initHandler() {
//         BluetoothDeviceServices.setActivityHandler(pHandler)
//     }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        val startBtn: ImageButton = binding.imgBtnStart;
        classicViewModel.startBtn.observe(viewLifecycleOwner) {
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


    private var pHandler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.MESSAGE_READ ->{
                updateDynamicTextView(it.obj.toString())
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
            Constants.CLIENT_SOCKET ->{
                if(it.obj == Constants.CONNECT){


                }
                else if(it.obj == Constants.DIS_CONNECT){

                }
            }
            Constants.CLIENT_SOCKET_ERROR -> {

            }
            Constants.CONNECTION_STOP ->{
                updateStartBtn()
                updateDeviceInfo()
                BlClassicDeviceServices.disconnectClientSocket()
                Toast.makeText(activity,
                    "Device is Disconnected",
                    Toast.LENGTH_SHORT).show();
            }
            Constants.CONNECTION_ERROR ->{
                updateStartBtn()
                BlClassicDeviceServices.disconnectClientSocket()
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
         var device = BlClassicDeviceServices.connDeviceInfo
         if(device != null){
             if(device.is_conn){
                 classicViewModel.deviceAddress.value = "Device: ${device.mac_addr}"
                 classicViewModel.deviceName.value = "Addr: ${device.name}"
             }
         }else{
             classicViewModel.deviceAddress.value = "Addr: YY"
             classicViewModel.deviceName.value = "Device: XX"
         }

     }


     private fun updateDynamicTextView(m:String) {
         println("Incomming string $m")
         var formatedText = m.replace("\n","").replace("\r","").replace(" ","").replace("?","")

         println("formated string $formatedText")
         var splitedString = formatedText.split(":")
         println("splited string $splitedString")

         if(splitedString.count() == 3){
             classicViewModel.recieveSpeed.value = "${splitedString[0]} : ${splitedString[1]}"
             classicViewModel.r_btn_obstacle.value = splitedString[2].contains("t")
         }
         else{
             classicViewModel.recieveSpeed.value = "00 : 00"
             classicViewModel.r_btn_obstacle.value = false
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

    }


    override fun onClick(v: View?) {
            if(v?.id == binding.imgBtnStart.id){
                if(dataTransferClass.getThreadStatus() == true){
                    stopDataTransfer()
//                    showDialogForStopDataTransfer("Device Connection","Do you want to close it ...?")
                }else{
                    startDataTransfer()
                }

            }
            else if(v?.id == binding.fBtnAcc.id){
                sendData("+1")
            }
            else if(v?.id == binding.fBtnDeAcc.id){
                sendData("-1")
            }
            else if(v?.id == binding.fBtnLeft.id){
                sendData("-1")
            }
            else if(v?.id == binding.fBtnRight.id){
                sendData("+1")
            }
    }





     fun sendData(s:String){
         dataTransferClass.sendCommand(s)
     }
    private fun startDataTransfer(){
        if(!BlClassicDeviceServices.getPermission()){
            Toast.makeText(activity,
                "Bluetooth permission is not set.",
                Toast.LENGTH_SHORT).show();

            return
        }

        if(!BlClassicDeviceServices.getBluetoothEnStatus()){
            Toast.makeText(activity,
                "Bluetooth is not turned on.",
                Toast.LENGTH_SHORT).show();
            return
        }

        val socket:BluetoothSocket? = BlClassicDeviceServices.getClientSocket()
        if(null == socket){
            Toast.makeText(activity,
                "Socket not created",
                Toast.LENGTH_SHORT).show();
            return
        }

        if(!socket.isConnected){
            Toast.makeText(activity,
                "Device is Disconnected",
                Toast.LENGTH_SHORT).show();
            return
        }


        dataTransferClass.setThread(socket,pHandler)
        dataTransferClass.startThread()

    }


    private fun stopDataTransfer(){

            dataTransferClass.cancelThread()
    }


     private fun updateStartBtn(){

         if(dataTransferClass.getThreadStatus() == true){
             //binding.imgBtnStart.tag="ON"
             classicViewModel.startBtn.value= true
         }else{
             //binding.imgBtnStart.tag="OFF"
             classicViewModel.startBtn.value = false
         }

//         try {
//             if(dataTransferClass.getThreadStatus() == true){
//                 //binding.imgBtnStart.tag="ON"
//                 homeViewModel.startBtn.value= true
//             }else{
//                 //binding.imgBtnStart.tag="OFF"
//                 homeViewModel.startBtn.value = false
//             }
//         }catch(e:Exception){
//             println("Error occured at updateBTn")
//         }


     }

//     private fun showDialogForStopDataTransfer(title:String, message:String){
//         if(this.activity != null && activity?.applicationContext != null){
//             val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!.applicationContext)
//             builder.setTitle(title)
//                 .setMessage(message)
//                 .setPositiveButton(android.R.string.ok) { _, _ ->
//                     stopDataTransfer()
//                 }.setNegativeButton(android.R.string.cancel){_,_ ->
//
//                 }
//
//             builder.create().show()
//         }
//         else{
//             println("Activity is null....")
//         }
//
//
//     }


 }



