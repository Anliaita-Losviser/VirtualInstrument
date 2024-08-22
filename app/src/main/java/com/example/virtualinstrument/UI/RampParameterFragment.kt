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

class RampParameterFragment : Fragment() {
    
    companion object {
        fun newInstance() = RampParameterFragment()
    }
    
    private val viewModel: RampParameterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Use the ViewModel
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ramp_parameter, container, false)
        val rampFloatingButton: FloatingActionButton = view.findViewById(R.id.rampParamButton)
        rampFloatingButton.setOnClickListener {
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