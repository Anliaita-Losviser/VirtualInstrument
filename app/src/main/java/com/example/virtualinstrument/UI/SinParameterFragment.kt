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