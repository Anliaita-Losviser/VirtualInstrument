/*
 * Copyright 2024 - 2025 华南理工大学 百步梯攀登计划项目组-低成本的高质量波形产生方案在电工电子仪器中的应用
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
 *
 * Disclaimer
 * This software is provided "as is" without any express or implied warranty,
 * including but not limited to the warranties of merchantability,
 * fitness for a particular purpose, and non-infringement.
 * The risk of using this software lies with the user. The developers or contributors shall not be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages resulting from the use of this software.
 * To the maximum extent permitted by law, the developers or contributors shall not be responsible for any claims,
 * losses, liabilities, damages, costs, or expenses arising from the use or inability to use this software.
 * 免责声明
 * 本软件按“现状”提供，不附带任何形式的明示或暗示保证，包括但不限于对适销性、特定用途的适用性或非侵权性的保证。
 * 使用本软件的风险由用户自行承担。开发者或贡献者不对因使用本软件而导致的任何直接、间接、偶然、特殊、惩戒性或后果性损害承担任何责任。
 * 在法律允许的最大范围内，开发者或贡献者对于因使用或无法使用本软件而产生的任何索赔、损失、责任、损害、成本或费用均不承担责任。
 */

package com.example.virtualinstrument.UI

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.example.virtualinstrument.R
import com.example.virtualinstrument.Utils.LogUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SinParameterFragment : Fragment() {
    
    companion object {
        fun newInstance() = SinParameterFragment()
    }
    
    private lateinit var viewModel: SinParameterViewModel
    var sinParamPrefer: SharedPreferences? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.w("Fragment","SinParameterFragment创建")
        //viewModel从配置项加载数据
        sinParamPrefer = activity?.getSharedPreferences("sinParameter",MODE_PRIVATE)
        var freq =""
        var range =""
        var offset =""
        var phase = ""
        sinParamPrefer?.let {
            freq = it.getString("Frequency","0").toString()
            range = it.getString("Range","0").toString()
            offset = it.getString("Offset","0").toString()
            phase = it.getString("Phase","0").toString()
        }
        viewModel = ViewModelProvider(this,
            SinParameterViewModelFactory(freq,range,offset,phase))
            .get(SinParameterViewModel::class.java)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sin_parameter, container, false)
        //设置悬浮按钮点击事件
        val sinFloatingButton: FloatingActionButton = view.findViewById(R.id.sinParamButton)
        sinFloatingButton.setOnClickListener{
            activity?.let { it1 ->
                AlertDialog.Builder(it1).apply {
                    setPositiveButton(R.string.confirm){ dialog, which->
                    }
                    setNegativeButton(R.string.cancel){ dialog, which->
                    }
                    show()
                }
            }
        }
        //输入频率
        val sinFreqEdit: EditText = view.findViewById(R.id.sinFreq)
        sinFreqEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                viewModel._freq.value = sinFreqEdit.text.toString()
                LogUtil.w("焦点变更","viewmodel:${viewModel._freq.value}")
            }
        }
        //输入幅度
        val sinRangeEdit: EditText = view.findViewById(R.id.sinRange)
        sinRangeEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                viewModel._range.value = sinRangeEdit.text.toString()
            }
        }
        //输入偏移
        val sinOffsetEdit: EditText = view.findViewById(R.id.sinOffset)
        sinOffsetEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                viewModel._offset.value = sinOffsetEdit.text.toString()
            }
        }
        //输入相位
        val sinPhaseEdit: EditText = view.findViewById(R.id.sinPhase)
        sinPhaseEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                viewModel._phase.value = sinPhaseEdit.text.toString()
            }
        }
        //给输入框设置初始值
        sinFreqEdit.setText(viewModel._freq.value)
        sinRangeEdit.setText(viewModel._range.value)
        sinOffsetEdit.setText(viewModel._offset.value)
        sinPhaseEdit.setText(viewModel._phase.value)
        return view
    }
    
    override fun onPause() {
        super.onPause()
        sinParamPrefer?.edit {
            //写入配置文件
            putString("Frequency", viewModel._freq.value)
            putString("Range", viewModel._range.value)
            putString("Offset", viewModel._offset.value)
            putString("Phase", viewModel._phase.value)
        }
    }
}