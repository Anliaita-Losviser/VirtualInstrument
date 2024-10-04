/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-10-02 19:43:38. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - socketTests.kt
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

import com.example.virtualinstrument.Utils.LogUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class socketTests {
    private var socket: Socket = Socket()
    private var isConnected: Boolean = false
    private lateinit var inStream: InputStream
    private lateinit var outStream: OutputStream

    fun checkConnected():Boolean{
        return if (isConnected)
            true
        else
            false
    }

    fun connect(socketAddress: String){
        thread {
            while (!isConnected) {
                try {
                    socket = Socket()
                    socket.connect(InetSocketAddress(socketAddress,6666))
                    isConnected = true
                    inStream = socket.getInputStream()
                    outStream = socket.getOutputStream()
                    LogUtil.i("socket连接","连接成功")
                }catch (e: SocketException) {
                    LogUtil.e("socket连接失败",e.toString())
                    socket.close()
                    Thread.sleep(500)
                }
            }
        }
    }

    fun close(){
        if (isConnected){
            try {
                inStream?.close()
                outStream?.close()
                socket.close()
                isConnected = false
                LogUtil.i("socket连接","关闭成功")
            } catch (e: SocketException){
                LogUtil.e("socket连接关闭失败",e.toString())
            }
        }
    }

    fun send(msg: String){
        thread {
            if (isConnected){
                outStream?.write(msg.toByteArray())
                outStream?.flush()
                LogUtil.i("socket数据","$msg 已发送")
            }
        }
    }

    fun receive():String{
        val bytes = ByteArray(1024)
        var str = ""
        try {
            inStream.read(bytes)
            str = String(bytes,charset("ascii"))
            //LogUtil.i("socket数据","接收到：$str")
            return str
        }catch (e:IOException){
            LogUtil.e("socket数据",e.toString())
            close()
            isConnected = false
            return str
        }
    }
}