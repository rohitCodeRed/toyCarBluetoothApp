package com.example.toycarbluetoothapp


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.toycarbluetoothapp.bluetooth.BluetoothConnectAsClientSocketThread
import com.example.toycarbluetoothapp.bluetooth.BluetoothDeviceServices
import com.example.toycarbluetoothapp.bluetooth.BluetoothModeChangeReceiver
import com.example.toycarbluetoothapp.bluetooth.Constants
import com.example.toycarbluetoothapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var blModeStatus :BluetoothModeChangeReceiver? = null
    private var blEnableIntent: Intent? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter?=null


    private var  socketCreationClass: BluetoothConnectAsClientSocketThread? = null

    val REQUEST_ENABLE_BT = 2
    val REQUEST_ENABLE_BT_OLDER = 1


    private var pHandler: Handler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.PERMISSION_HANDLER -> {
                if(it.obj == 1){
                    //startDeviceDiscovery()
                }
                println("Permission granted. handler...${it.obj}\n")
            }
            Constants.BL_HANDLER ->{
                if(it.obj == 1){
                    //startDeviceDiscovery()
                }

                println("Bluetooth Enabled handler....${it.obj}\n")
            }
        }
        return@Handler false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_control, R.id.nav_connect, R.id.nav_device_info
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        initHelper()

        initSocketCreateClass()

        initBluetoothInstances()

        checkMultiplePermissions()

        checkBlStatusOnStart()

        initBroadcastReceiver()


    }

    private fun initHelper() {
       BluetoothDeviceServices.setActivityHandler(pHandler)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
           R.id.action_blActivity ->{

                   val intent = Intent (this, ListBluetoothDevices::class.java)
               println("Menu selected....")

//               if(bluetoothAdapter!!.isDiscovering){
//                   var stq =  bluetoothAdapter!!.cancelDiscovery()
//                   println("Cancel dicovering, ${stq}")
//               }
//               else{
//                    var st =  bluetoothAdapter?.startDiscovery()
//                    println("Discovering.....: ${st}")
//               }


               startActivity(intent)

           }
            R.id.action_enablePerm->{
                checkMultiplePermissions()

            }
            R.id.action_enableBl->{
                enableBluetooth()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        println("In menu enable perm")
        menu.children.forEach {
            when(it.itemId){
                R.id.action_enablePerm->{
                    if(BluetoothDeviceServices.getPermission()){

                        it.setChecked(true)
                    }
                    else{
                        //it.icon = null
                        it.setChecked(false)
                    }

                }
                R.id.action_enableBl->{
                    if(BluetoothDeviceServices.getBluetoothEnStatus()){

                        it.setChecked(true)
                    }
                    else{
                        it.setChecked(false)
                    }
                }
            }

        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(blModeStatus)
    }



    private fun initBluetoothInstances(){
        bluetoothManager = getSystemService(BluetoothManager::class.java)

        bluetoothAdapter = bluetoothManager!!.adapter
        blEnableIntent =  Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

//        BluetoothDevManager.setActivityHandler(pHandler)
        BluetoothDeviceServices.setBluetoothAdapter(bluetoothAdapter!!)
    }

    private fun initSocketCreateClass(){
        socketCreationClass = BluetoothConnectAsClientSocketThread()
        BluetoothDeviceServices.setSocketCreateClass(socketCreationClass)
    }


    private fun checkBlStatusOnStart(){
        if(bluetoothManager!!.adapter == null){
            println("doenot support bluetooth")
            Toast.makeText(this, "Does not support bluetooth..!", Toast.LENGTH_SHORT).show();
            return
        }

        if (bluetoothAdapter!!.isEnabled) {

            println("Bluetooth on from initial update..")
            BluetoothDeviceServices.setBluetoothEnable(true);
            //pHandler.obtainMessage(Constants.BL_HANDLER,1,).sendToTarget()
        }else{
            BluetoothDeviceServices.setBluetoothEnable(false);
            //pHandler.obtainMessage(Constants.BL_HANDLER,0,).sendToTarget()
        }

    }

    private fun initBroadcastReceiver(){
        blModeStatus = BluetoothModeChangeReceiver()

        val filterAF = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(blModeStatus, filterAF)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(blModeStatus, filter)

        val discFilterS = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        registerReceiver(blModeStatus, discFilterS)

//        val discFilterE = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        registerReceiver(blModeStatus, discFilterE)
    }


    private fun checkMultiplePermissions(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) + ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) + ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) + ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                )) != PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.BLUETOOTH_ADMIN
                    )
                ) {
                    showPermissionDialog(
                        "Permission for Bluetooth Needed",
                        "Rationale",
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ),
                        REQUEST_ENABLE_BT
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        ), REQUEST_ENABLE_BT
                    )
                }

            }
            else{
                BluetoothDeviceServices.setPermissionGrant(true)
            }
        }

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) + ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED)
            {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH_ADMIN)) {
                    showPermissionDialog(
                        "Permission for BLUETOOTH Needed",
                        "Rationale",
                        arrayOf(Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN),
                        REQUEST_ENABLE_BT_OLDER
                    )
                }
                else{
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN), REQUEST_ENABLE_BT_OLDER)
                }

            }
            else{
                BluetoothDeviceServices.setPermissionGrant(true)
            }
        }
    }

    private fun enableBluetooth(){

        if(!BluetoothDeviceServices.getPermission()){
            Toast.makeText(this, "Please Enable Bluetooth Permissions first.", Toast.LENGTH_SHORT).show();
            return
        }

        if(bluetoothManager?.adapter == null){
            println("doenot support bluetooth")
            Toast.makeText(this, "Does not support bluetooth..!", Toast.LENGTH_SHORT).show();
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            if(blEnableIntent != null){
                startActivityForResult(blEnableIntent!!, REQUEST_ENABLE_BT)
            }
            //registerForActivityResult()
            return
        }

        BluetoothDeviceServices.setBluetoothEnable(true);


    }


    private fun showPermissionDialog(title:String, message:String, perm_name:Array<String>, perm_code:Int){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ActivityCompat.requestPermissions(this, perm_name, perm_code)
            }

        builder.create().show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == REQUEST_ENABLE_BT){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission for BT Granted! for code : $REQUEST_ENABLE_BT", Toast.LENGTH_SHORT).show();

                BluetoothDeviceServices.setPermissionGrant(true);
//                bluetoothAdapter?.cancelDiscovery()
//                var st =  bluetoothAdapter?.startDiscovery()
//                println("Discovering.....: ${st}")
            }
            else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();

                BluetoothDeviceServices.setPermissionGrant(false);
            }
        }
        else if(requestCode == REQUEST_ENABLE_BT_OLDER){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission for BT_SCAN Granted!", Toast.LENGTH_SHORT).show();

                BluetoothDeviceServices.setPermissionGrant(true);
            }
            else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                BluetoothDeviceServices.setPermissionGrant(false);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){

                BluetoothDeviceServices.setBluetoothEnable(true)
                //startDeviceDiscovery()
                println("User pressed ok button: onActivityResult()")

            }else if(resultCode == Activity.RESULT_CANCELED){

                BluetoothDeviceServices.setBluetoothEnable(false)
                println("User pressed No button :onActivityResult()")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

}
















//Manually created slide menu..
/*

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,binding.appBarMain.toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        //navView.setNavigationItemSelectedListener(this)
        navView.setNavigationItemSelectedListener(this)




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)

        }else{
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when(id){
            R.id.nav_control ->{
                loadFragment(ControlFragment())
            }
            R.id.nav_connect ->{

            }
            R.id.nav_device_info ->{

            }
            else->{

            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }

    private fun loadFragment(fr:Fragment) {
        val fm:FragmentManager = supportFragmentManager
        val ft:FragmentTransaction = fm.beginTransaction()

        ft.add(R.id.nav_host_fragment_content_main,fr)
        ft.commit()
    }


}

 */






