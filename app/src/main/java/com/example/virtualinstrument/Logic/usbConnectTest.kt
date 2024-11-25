/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-10-06 00:26:29. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - usbConnectTest.kt
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

import android.app.PendingIntent
import android.hardware.usb.UsbManager
import com.example.virtualinstrument.Utils.LogUtil
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlin.concurrent.thread


class usbConnectTest {
    private lateinit var port: UsbSerialPort

    fun connect(manager: UsbManager,USBIntent: PendingIntent){

            LogUtil.i("usb","准备连接")
            val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            if (availableDrivers.isEmpty()) {
                LogUtil.i("usb","无可用设备")
                return
            }

            // Open a connection to the first available driver.
            val driver = availableDrivers[0]
            val connection = manager.openDevice(driver.device)
            if (connection == null) {
                LogUtil.i("usb","连接不成功")
                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                manager.requestPermission(driver.device,USBIntent)
                return
            }

            port = driver.ports[0] // Most devices have just one port (port 0)
            port.open(connection)
            LogUtil.i("usb","连接打开")
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

    }

    fun close(){
        if(port.isOpen) {
            LogUtil.i("usb","连接关闭")
            port.close()
        }
    }

    fun send(){
        if(port.isOpen){
            LogUtil.i("usb","准备发送数据")
            var msg = "l"
            for (i in 1..64){
                val l1="111"
                msg += l1
            }
            LogUtil.w("usb",msg)
            port.write(msg.toByteArray(),100)
        }
    }
}