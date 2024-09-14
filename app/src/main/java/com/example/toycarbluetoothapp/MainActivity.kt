package com.example.toycarbluetoothapp


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.toycarbluetoothapp.bluetooth.BLeDeviceServices
import com.example.toycarbluetoothapp.bluetooth.BlClassicConnectAsClientSocketThread
import com.example.toycarbluetoothapp.bluetooth.BlClassicDeviceServices
import com.example.toycarbluetoothapp.bluetooth.BlBroadcastReceiver
import com.example.toycarbluetoothapp.bluetooth.BleDeviceListServices
import com.example.toycarbluetoothapp.databinding.ActivityMainBinding
import com.example.toycarbluetoothapp.ui.ble.BleFragment
import com.example.toycarbluetoothapp.ui.classic.ClassicFragment
import com.example.toycarbluetoothapp.ui.deviceinfo.DeviceInfoFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    DrawerListener {
    private val TAG = "MainActivity"
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var blModeStatus :BlBroadcastReceiver? = null
    private var blEnableIntent: Intent? = null
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter?=null


    private var  socketCreationClass: BlClassicConnectAsClientSocketThread? = null

    private var fragmentMap:Map<String,Int> = mapOf<String,Int>()

    val REQUEST_ENABLE_BT = 2
    val REQUEST_ENABLE_BT_OLDER = 1

    private lateinit var frManager:FragmentManager

    private var pHandler: Handler = Handler(Looper.myLooper()!!){
        when(it.what){
            Constants.PERMISSION_HANDLER -> {
                if(it.obj == 1){
                    //startDeviceDiscovery()
                }
                println("$TAG: Permission granted..\n")
            }
            Constants.BL_HANDLER ->{
                if(it.obj == 1){
                    //startDeviceDiscovery()
                }

                println("$TAG: Bluetooth Enabled..\n")
            }
        }
        return@Handler false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        initFrManger()

        initFragmentNavigationMap()

        initNavigationLayout()

        loadInitialVisibleFragment()

        initBackPressedAction()

        initHelper()

        initSocketCreateClass()

        initBluetoothInstances()

        checkMultiplePermissions()

        checkBlStatusOnStart()

        initBroadcastReceiver()


    }


    private fun initFrManger(){
        frManager = supportFragmentManager
        frManager.addOnBackStackChangedListener(frListener)
    }


    private val frListener = FragmentManager.OnBackStackChangedListener{
        var fr:Fragment?
        fragmentMap.forEach {
           fr = frManager.findFragmentByTag(it.key)
            Constants.VISIBLE_FRAGMENT[it.key] = fr?.isVisible == true
        }
    }

    private fun initFragmentNavigationMap(){
        fragmentMap = mapOf(Pair(Constants.HOME_FRAGMENT,R.id.nav_classic),
            Pair(Constants.BLE_FRAGMENT,R.id.nav_ble),
            Pair(Constants.DEVICE_INFO,R.id.nav_device_info))
    }

    private fun initNavigationLayout(){
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val toggle = ActionBarDrawerToggle(this,drawerLayout,binding.appBarMain.toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(this)

        toggle.syncState();

        navView.setNavigationItemSelectedListener(this)


    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    override fun onDrawerOpened(drawerView: View) {
        Constants.VISIBLE_FRAGMENT.forEach {
            if(it.value) {
                val id:Int? = fragmentMap.get(it.key)
                if(id != null){
                    findViewById<NavigationView>(R.id.nav_view).menu.findItem(id).setChecked(true)
                }
            }
        }

    }

    override fun onDrawerClosed(drawerView: View) {

    }

    override fun onDrawerStateChanged(newState: Int) {

    }


    private fun loadInitialVisibleFragment(){

        loadFragmentWithNavigation(ClassicFragment(),Constants.HOME_FRAGMENT,Constants.FRAGMENT_FLAG_ADD)

        Constants.VISIBLE_FRAGMENT.forEach { t, u ->
            if(u){
                when(t){
                    Constants.HOME_FRAGMENT->{
                        loadFragmentWithNavigation(ClassicFragment(),Constants.HOME_FRAGMENT,Constants.FRAGMENT_FLAG_ADD)
                    }
                    Constants.BLE_FRAGMENT->{
                        loadFragmentWithNavigation(BleFragment(),Constants.BLE_FRAGMENT,Constants.FRAGMENT_FLAG_REPLACE)
                    }
                    Constants.DEVICE_INFO->{
                        loadFragmentWithNavigation(DeviceInfoFragment(),Constants.DEVICE_INFO,Constants.FRAGMENT_FLAG_REPLACE)
                    }
                }
            }
        }

    }

    private fun loadFragmentWithNavigation(fr:Fragment,code:String,flag:Int){
        loadFragment(fr,code,flag)
        binding.navView.menu.findItem(fragmentMap.get(code)!!).setChecked(true)
    }


    private fun initBackPressedAction(){
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val mFragment: Fragment? = supportFragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT)
            if (mFragment != null && mFragment.isVisible) {

                finish()
            }
            else{
                supportFragmentManager.popBackStack()
            }

            if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
                binding.drawerLayout.closeDrawer(GravityCompat.START)

            }
        }
    }





    private fun initHelper() {
       BlClassicDeviceServices.setActivityHandler(pHandler)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
           R.id.action_blActivity ->{
               val intent = Intent (this, BlClassicDevicesListActivity::class.java)
               startActivity(intent)
           }
            R.id.action_bleScan ->{
//                val intent = Intent (this, BleDeviceListActivity::class.java)
//                startActivity(intent)

                val intent = Intent (this, AllBluetoothDeviceListActivity::class.java)
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


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id){
            R.id.nav_classic ->{
                loadFragment(ClassicFragment(),Constants.HOME_FRAGMENT,Constants.FRAGMENT_FLAG_ADD)
            }
            R.id.nav_ble ->{
                loadFragment(BleFragment(),Constants.BLE_FRAGMENT,Constants.FRAGMENT_FLAG_REPLACE)
            }
            R.id.nav_device_info ->{
                loadFragment(DeviceInfoFragment(),Constants.DEVICE_INFO,Constants.FRAGMENT_FLAG_REPLACE)
            }
            else->{

            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun loadFragment(fr: Fragment,tag:String?,flag:Int) {
        val fm: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()

        if(flag == Constants.FRAGMENT_FLAG_ADD){
            ft.add(R.id.nav_host_fragment_content_main,fr,tag)
            fm.popBackStack(Constants.HOME_FRAGMENT,FragmentManager.POP_BACK_STACK_INCLUSIVE)
            ft.addToBackStack(Constants.HOME_FRAGMENT)
        }
        else if(flag == Constants.FRAGMENT_FLAG_REPLACE){
            ft.replace(R.id.nav_host_fragment_content_main,fr,tag)
            ft.addToBackStack(null)
        }

        ft.commit()

    }



    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
//        println("In menu enable perm")
        menu.children.forEach {
            when(it.itemId){
                R.id.action_enablePerm->{
                    if(BlClassicDeviceServices.getPermission()){

                        it.setChecked(true)
                    }
                    else{
                        //it.icon = null
                        it.setChecked(false)
                    }

                }
                R.id.action_enableBl->{
                    if(BlClassicDeviceServices.getBluetoothEnStatus()){

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

        BlClassicDeviceServices.setBluetoothAdapter(bluetoothAdapter!!)
        BleDeviceListServices.setAdaptor(bluetoothAdapter)
    }

    private fun initSocketCreateClass(){
        socketCreationClass = BlClassicConnectAsClientSocketThread()
        BlClassicDeviceServices.setSocketCreateClass(socketCreationClass)
    }


    private fun checkBlStatusOnStart(){
        if(bluetoothManager!!.adapter == null){
            Toast.makeText(this, "Does not support bluetooth..!", Toast.LENGTH_SHORT).show();
            return
        }

        if (bluetoothAdapter!!.isEnabled) {
            BlClassicDeviceServices.setBluetoothEnable(true);
            BleDeviceListServices.setBluetoothStatus(true)

        }else{
            BlClassicDeviceServices.setBluetoothEnable(false);
            BleDeviceListServices.setBluetoothStatus(false)
        }

    }

    private fun initBroadcastReceiver(){
        blModeStatus = BlBroadcastReceiver()

        val filterAF = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(blModeStatus, filterAF)

//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(blModeStatus, filter)
//
//        val discFilterS = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//        registerReceiver(blModeStatus, discFilterS)

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
                BlClassicDeviceServices.setPermissionGrant(true)
                BleDeviceListServices.setPermissionStatus(true)
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
                BlClassicDeviceServices.setPermissionGrant(true)
                BleDeviceListServices.setPermissionStatus(true)
            }
        }
    }

    private fun enableBluetooth(){

        if(!BlClassicDeviceServices.getPermission()){
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

        BlClassicDeviceServices.setBluetoothEnable(true)
        BleDeviceListServices.setBluetoothStatus(true)


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

                BlClassicDeviceServices.setPermissionGrant(true);
                BleDeviceListServices.setPermissionStatus(true)
            }
            else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();

                BlClassicDeviceServices.setPermissionGrant(false);
                BleDeviceListServices.setPermissionStatus(false)
            }
        }
        else if(requestCode == REQUEST_ENABLE_BT_OLDER){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission for BT_SCAN Granted!", Toast.LENGTH_SHORT).show();

                BlClassicDeviceServices.setPermissionGrant(true);
                BleDeviceListServices.setPermissionStatus(true)
            }
            else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                BlClassicDeviceServices.setPermissionGrant(false);
                BleDeviceListServices.setPermissionStatus(false)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){

                BlClassicDeviceServices.setBluetoothEnable(true)
                BleDeviceListServices.setBluetoothStatus(true)
                //startDeviceDiscovery()
                println("$TAG: User pressed ok button")

            }else if(resultCode == Activity.RESULT_CANCELED){

                BlClassicDeviceServices.setBluetoothEnable(false)
                BleDeviceListServices.setBluetoothStatus(false)
                println("$TAG: User pressed No button")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }




}



//Nav Graph navigation view
/*
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
        navView.setNavigationItemSelectedListener(this)


//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }

*/



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






