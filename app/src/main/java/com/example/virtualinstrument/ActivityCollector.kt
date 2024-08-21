/*
 * Copyright (c) 华南理工大学学生创新团队 Last Update: 2024-08-19 14:32:24. All Rights Reserved.
 *
 * @Project name and File name:VirtualInstrument - ActivityCollector.kt
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

import android.app.Activity

object ActivityCollector {
    private val activitys = ArrayList<Activity>()
    
    fun addActivity(activity: Activity){
        activitys.add(activity)
    }
    
    fun removeActivity(activity: Activity){
        activitys.remove(activity)
    }
    
    fun finishAllActivity(){
        for (activity in activitys){
            if (!activity.isFinishing){
                activity.finish()
            }
        }
        activitys.clear()
    }
}