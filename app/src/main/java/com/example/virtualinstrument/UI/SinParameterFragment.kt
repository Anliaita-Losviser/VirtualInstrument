package com.example.virtualinstrument.UI

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.virtualinstrument.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SinParameterFragment : Fragment() {
    
    companion object {
        fun newInstance() = SinParameterFragment()
    }
    
    private val viewModel: SinParameterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Use the ViewModel
        
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
        return view
    }
}