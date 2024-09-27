package com.example.virtualinstrument.UI

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SinParameterViewModel(freq: String,range: String,offset: String,phase: String) : ViewModel() {
    //保存输入的数据
    var _freq = MutableLiveData<String>()
    var _range = MutableLiveData<String>()
    var _offset = MutableLiveData<String>()
    var _phase = MutableLiveData<String>()
    init {
        _freq.value = freq
        _range.value = range
        _offset.value = offset
        _phase.value = phase
    }
}

class SinParameterViewModelFactory(private val freq: String="", private val range: String="",
                                   private val offset: String="", private val phase: String="")
    : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SinParameterViewModel(freq,range,offset,phase) as T
    }
}