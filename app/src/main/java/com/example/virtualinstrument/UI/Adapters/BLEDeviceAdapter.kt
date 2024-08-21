/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-08-19 19:01:30. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - BluetoothDeviceAdapter.kt
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

package com.example.virtualinstrument.UI.Adapters

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualinstrument.R
import com.example.virtualinstrument.Utils.LogUtil
import com.example.virtualinstrument.VIApp

class BLEDeviceAdapter(val deviceList: List<BluetoothDevice>): RecyclerView.Adapter<BLEDeviceAdapter.ViewHolder>(){
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
            val device = deviceList[position]
            if (ActivityCompat.checkSelfPermission(
                    VIApp.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                LogUtil.w("点击事件","位置:${position}, 名称:${device.name}")
            }
        }
        return viewHolder//缓存布局
    }
    
    override fun getItemCount() = deviceList.size
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val device = deviceList[position]
        if (ActivityCompat.checkSelfPermission(
                VIApp.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            holder.deviceName.text = device.name
        }
    }
}