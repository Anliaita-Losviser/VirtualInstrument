/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-08-21 20:08:12. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - BLEConnectionService.kt
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

package com.example.virtualinstrument.Logic

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.virtualinstrument.MainActivity
import com.example.virtualinstrument.Utils.LogUtil
import com.example.virtualinstrument.VIApp
import java.util.UUID
import kotlin.concurrent.thread

class BLEConnectionService : Service() {
    lateinit var selectedBLEDevice: BluetoothDevice//选中的设备
    var BLEDeviceGatt: BluetoothGatt? = null//GATT对象
    var targetVIService: BluetoothGattService? = null//GATT服务
    var targetCha: BluetoothGattCharacteristic? = null//GATT特征
    private val mBinder = BLEBinder()
    
    inner class BLEBinder: Binder(){
        fun startConnection(device: BluetoothDevice){
            LogUtil.i("服务","开始连接")
            selectedBLEDevice = device
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                LogUtil.w("点击","名称:${selectedBLEDevice.name}")
                LogUtil.w("点击","地址:${selectedBLEDevice.address}")
                //开启子线程准备进行连接
                thread {
                    BLEDeviceGatt = selectedBLEDevice.connectGatt(VIApp.context,false,gattCallback)
                }
            }
        }
        fun endConnection(){
            LogUtil.i("服务","结束连接")
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                BLEDeviceGatt?.disconnect()
                BLEDeviceGatt?.close()
                BLEDeviceGatt = null
            }
        }
        fun sendMessage(){
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                LogUtil.i("服务","发送命令")
                val openVICommand = "open"
                val transToByteArr = openVICommand.toByteArray()
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    targetCha?.let {
                        if (BLEDeviceGatt?.writeCharacteristic(it,
                                transToByteArr,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothStatusCodes.SUCCESS){
                            LogUtil.i("服务","发送成功")
                        }
                    }
                }else{
                    if (targetCha?.setValue(transToByteArr) == true){
                        LogUtil.i("服务","发送成功")
                    }
                }
            }
        }
    }
    //GATT服务回调
    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                LogUtil.w("连接结果","连接成功")
                if (ActivityCompat.checkSelfPermission(
                        VIApp.context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    gatt?.discoverServices()//连接成功后搜索服务
                }
                
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                LogUtil.w("连接结果","连接失败")
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //服务列表
                val services: MutableList<BluetoothGattService>? = gatt?.services
                if (services != null) {
                    for (service in services) {
                        LogUtil.w("查找服务UUID","UUID:${service.uuid}")
                    }
                    //获取服务
                    targetVIService = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
                    if(targetVIService != null){
                        LogUtil.w("获取对应服务","目标服务UUID:${targetVIService!!.uuid}")
                        //获取特征列表
                        val chas = targetVIService!!.characteristics
                        if(chas != null){
                            for (cha in chas) {
                                LogUtil.w("获取特征","目标特征UUID:${cha.uuid}")
                                //获取目标特征
                                targetCha = targetVIService!!.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"))
                            }
                        }
                    }else{
                        LogUtil.e("获取对应服务","获取服务失败")
                    }
                }
            }
            else{
                LogUtil.e("查找服务UUID","无服务")
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        LogUtil.i("服务","蓝牙服务创建")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("BLEService","Service通知",
                NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        //创建pendingIntent
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this,0,
            intent,PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this,"BLEService")
            .setContentTitle("蓝牙服务")
            .setContentText("蓝牙服务正在运行")
            .setContentIntent(pi)
            .build()
        startForeground(1,notification)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtil.i("服务","蓝牙服务启动")
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i("服务","蓝牙服务销毁")
    }
}