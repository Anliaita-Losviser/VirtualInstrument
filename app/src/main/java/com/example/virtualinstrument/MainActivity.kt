/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-08-09 18:34:38. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - MainActivity.kt
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 重新分发和使用源代码和二进制形式的代码，无论是否进行修改，都是允许的，只要满足以下条件：
 * 重新分发源代码时，必须保留上述版权通知、本条件列表以及以下免责声明。
 * 以二进制形式重新分发时，必须在分发时提供的文档或其他材料中复制上述版权通知、本条件列表以及以下免责声明。
 * 未经事先书面许可，不得使用开发者或贡献者的名称来认可或推广从本软件派生出来的产品。
 */

package com.example.virtualinstrument

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualinstrument.Common.IntentRequestCodes
import com.example.virtualinstrument.Logic.BLEConnectionService
import com.example.virtualinstrument.Logic.socketTests
import com.example.virtualinstrument.UI.SinParameterFragment
import com.example.virtualinstrument.Utils.LogUtil
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import org.json.JSONArray
import java.util.Arrays


class MainActivity : BaseActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    //设备列表
    private val bleDeviceList = ArrayList<BluetoothDevice>()
    //绑定服务
    lateinit var BLEServiceBinder: BLEConnectionService.BLEBinder
    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //初始化binder
            BLEServiceBinder = service as BLEConnectionService.BLEBinder
        }
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
    
    val layoutManager = LinearLayoutManager(VIApp.context)
    lateinit var layoutAdapter: BLEDeviceAdapter
    lateinit var bleDeviceDialog: AlertDialog
    lateinit var ipDeviceDialog: AlertDialog
    private var ipDeviceAddress:String = ""
    //蓝牙适配器实例
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var BLEScanner: BluetoothLeScanner//扫描器
    private var scanning = false
    private val handler = Handler()
    // Stops scanning after 10 seconds.
    private val scanPeriod: Long = 10000

    private val socketTest = socketTests()
    lateinit var updateUITask: UpdateUIAsyncTask
    lateinit var echarts:WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //检查设备是否支持BLE
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(VIApp.context, "设备不支持蓝牙BLE，将关闭", Toast.LENGTH_SHORT).show()
            ActivityCollector.finishAllActivity()
        }

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        BLEScanner = bluetoothAdapter.bluetoothLeScanner

        XXPermissions.with(this)
            // 申请蓝牙权限
            .permission(Permission.Group.BLUETOOTH)
            // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        LogUtil.i("权限","部分授权未授予")
                        return
                    }
                    LogUtil.i("权限","授权完成")
                }
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        LogUtil.i("权限","被永久拒绝授权，请手动授予")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(VIApp.context, permissions)
                    } else {
                        LogUtil.i("权限","获取权限失败")
                    }
                }
            })

        //绑定服务并自动创建
        val serviceIntent = Intent(this, BLEConnectionService::class.java)
        bindService(serviceIntent,connection,Context.BIND_AUTO_CREATE)
        
        layoutAdapter = BLEDeviceAdapter(bleDeviceList)
        //初始化自定义AlertDialog
        bleDeviceDialog = createBluetoothDevicesDialog(this)
        //读取ip数据
        val pref = this.getSharedPreferences("instrumentPref", Context.MODE_PRIVATE)
        ipDeviceAddress = pref?.getString("ipDeviceAddress", "").toString()
        LogUtil.i("读缓存",ipDeviceAddress)
        //初始化ip连接弹窗
        ipDeviceDialog = createIPDeviceDialog(this)


        //初始化工具栏
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //加载参数区
        replaceFragment(SinParameterFragment())
        //显示图表
        echarts = findViewById(R.id.echarts)
        echarts.settings.javaScriptEnabled = true
        echarts.settings.javaScriptCanOpenWindowsAutomatically = true
        echarts.settings.domStorageEnabled = true
        echarts.settings.loadsImagesAutomatically = true
        echarts.settings.mediaPlaybackRequiresUserGesture = true
        echarts.settings.allowFileAccess = true
        echarts.webViewClient = WebViewClient()
        echarts.loadUrl("file:///android_asset/index.html")
    }

    override fun onPause() {
        super.onPause()
        //保存数据
        LogUtil.i("onPause", "onPause执行")
        val editor = this.getSharedPreferences("instrumentPref", Context.MODE_PRIVATE).edit()
        editor.putString("ipDeviceAddress",ipDeviceAddress)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
    //切换Fragment
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        //开启一个事务
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.ParaArea,fragment)
        transaction.commit()
    }
    //设置tooBar菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        menuInflater.inflate(R.menu.toolbar , menu)
        return true
    }
    //tooBar菜单的点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.itemId) {
            //断开连接按钮点击事件
            R.id.disConnect ->{
                updateUITask?.cancel(true)
                socketTest.close()
                Toast.makeText(VIApp.context, R.string.disconnect_tip, Toast.LENGTH_SHORT).show()
            }
            //连接按钮点击事件
            R.id.Connect -> {
                //弹出IP地址输入框
                ipDeviceDialog.show()
            }
            R.id.Select -> {
                //初始化异步任务
                updateUITask = UpdateUIAsyncTask()
                updateUITask?.execute()
            }
        }
        return true
    }
    //检查蓝牙是否打开
    private fun CheckBTOpen(){
        if(ContextCompat.checkSelfPermission(VIApp.context,
                Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, IntentRequestCodes.REQUEST_ENABLE_BT)
            }
        }
    }
    //扫描设备
    private fun ScanDevices(){
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                if (ActivityCompat.checkSelfPermission(
                        VIApp.context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    scanning = false
                    BLEScanner.stopScan(leScanCallback)
                    LogUtil.i("扫描","10秒取消扫描")
                }
            }, scanPeriod)
            
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {//开始扫描
                bleDeviceList.clear()
                handler.postDelayed({
                    scanning = true
                    BLEScanner.startScan(leScanCallback)
                    LogUtil.i("扫描","开始扫描")
                },500)
            }
        } else {
            scanning = false
            BLEScanner.stopScan(leScanCallback)
            LogUtil.i("扫描","停止扫描")
        }
    }
    //构造自定义的AlertDialog
    private fun createBluetoothDevicesDialog(context: Context):AlertDialog {
        //获取构造器
        val dialogBuilder = AlertDialog.Builder(context)
        // Inflate the custom layout
        val deviceListView = LayoutInflater.from(context).inflate(R.layout.bluetooth_device_list, null)
        val recyclerview: RecyclerView = deviceListView.findViewById(R.id.device_list)
        val scanBLEButton: Button = deviceListView.findViewById(R.id.scan_device)
        // Setup RecyclerView
        recyclerview.layoutManager = layoutManager
        recyclerview.adapter = layoutAdapter
        // Setup the dialog
        dialogBuilder.setView(deviceListView)
            .setTitle("蓝牙设备")
        scanBLEButton.setOnClickListener{
            //执行蓝牙扫描操作
            ScanDevices()
        }
        // Create and show the dialog
        val dialog = dialogBuilder.create()
        return dialog
    }

    //构建IP连接弹窗
    private fun createIPDeviceDialog(context: Context):AlertDialog{
        //获取构造器
        val dialogBuilder = AlertDialog.Builder(context)
        // Inflate the custom layout
        val ipDeviceView = LayoutInflater.from(context).inflate(R.layout.ip_device_connect, null)
        val ipAddressEditText: EditText = ipDeviceView.findViewById(R.id.ipaddress)
        val confirm_connect:Button = ipDeviceView.findViewById(R.id.confirm_connect)
        val cancel_connect:Button = ipDeviceView.findViewById(R.id.cancel_connect)
        // Setup the dialog
        dialogBuilder.setView(ipDeviceView)
            .setTitle("IP连接")
        // Create and show the dialog
        val dialog = dialogBuilder.create()
        //设置初始值
        ipAddressEditText.setText(ipDeviceAddress)
        //确认并发起连接
        confirm_connect.setOnClickListener {
            ipDeviceAddress = ipAddressEditText.text.toString()
            //连接IP
            socketTest.connect(ipAddressEditText.text.toString())
            dialog.hide()
        }
        cancel_connect.setOnClickListener {
            ipDeviceAddress = ipAddressEditText.text.toString()
            dialog.hide()
        }
        return dialog
    }
    //蓝牙扫描的结果回调函数
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            //获取蓝牙设备
            val realDevice = result.device
            realDevice?.let {
                if (ActivityCompat.checkSelfPermission(
                        VIApp.context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //调试信息
                    LogUtil.w("扫描","实际设备名称：${it.name}")
                    if (it.uuids != null){
                        for(serviceUuid in it.uuids){
                            LogUtil.w("serviceData: ", serviceUuid.toString())
                        }
                    }else{
                        LogUtil.w("serviceData: ","没有serviceData")
                    }
                    //添加设备并刷新列表
                    if(!bleDeviceList.contains(it) && (it.name != null)){
                        bleDeviceList.add(it)
                        layoutAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    //AsyncTask,刷新ui数据
    inner class UpdateUIAsyncTask: AsyncTask<Unit, Array<Double>, Unit>(){
        override fun onPreExecute() {
            super.onPreExecute()
            LogUtil.i("AsyncTask","任务开始执行")
        }

        override fun doInBackground(vararg params: Unit?) {
            LogUtil.i("AsyncTask","任务正在执行")
            //获取socket数据
            var mstr = ""
            while (socketTest.checkConnected()){
                mstr = socketTest.receive()
                LogUtil.i("AsyncTask","接收到：$mstr")
                // 正则表达式匹配带有符号的数字
                val regex = Regex("""([+-]?\d*\.?\d+)""")
                // 查找所有匹配项
                val matches = regex.findAll(mstr).map { it.value.toDouble() }.toMutableList()
                // 将 ArrayList 转换为 Array
                val matchesArray = matches.toTypedArray()
                println("数组元素为:${matchesArray.contentToString()}")
                publishProgress(matchesArray)
            }
        }

        override fun onProgressUpdate(vararg values: Array<Double>?) {
            super.onProgressUpdate(*values)
            //更新ui
            // 假设你有一个数组arrayToPass
            //val arrayToPass = arrayOf("0.0", "0.8", "1.2")
            //val arrayToPass = arrayOf(0.0, 0.8, 1.2)
            // 将数组转换为JSON字符串
            LogUtil.e("json","json原始数据:${values[0].contentToString()}")
            val jsonArray = JSONArray(values[0])
            val jsonString = jsonArray.toString()

            LogUtil.e("webview", jsonString)

            //echarts.evaluateJavascript("javascript:switchToRamp()",null)
            echarts.evaluateJavascript("javascript:accept('"+ jsonString +"')",null)
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            LogUtil.i("AsyncTask","任务执行完毕")
        }

        override fun onCancelled() {
            LogUtil.i("AsyncTask","任务被取消")
            super.onCancelled()
        }
    }

    //recyclerView适配器
    inner class BLEDeviceAdapter(val bleDeviceList: List<BluetoothDevice>): RecyclerView.Adapter<BLEDeviceAdapter.ViewHolder>(){
        //用来缓存信息的内部类
        inner class ViewHolder(deviceItemView: View) : RecyclerView.ViewHolder(deviceItemView){
            val deviceName: TextView = deviceItemView.findViewById(R.id.device_name)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val View = LayoutInflater.from(parent.context)
                .inflate(R.layout.bluetooth_device_item, parent, false)
            val viewHolder = ViewHolder(View)
            //给子项设置点击事件
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                if (ActivityCompat.checkSelfPermission(
                        VIApp.context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //启动连接操作
                    BLEScanner.stopScan(leScanCallback)
                    LogUtil.i("扫描","停止扫描")
                    BLEServiceBinder.startConnection(bleDeviceList[position])
                }
            }
            return viewHolder//缓存布局
        }
        
        override fun getItemCount() = bleDeviceList.size
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int){
            val device = bleDeviceList[position]
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                holder.deviceName.text = device.name
            }
        }
    }
}