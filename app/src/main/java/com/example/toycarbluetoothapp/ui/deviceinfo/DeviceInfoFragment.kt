package com.example.toycarbluetoothapp.ui.deviceinfo

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.toycarbluetoothapp.databinding.FragmentDeviceInfoBinding

class DeviceInfoFragment : Fragment() {

    private var _binding: FragmentDeviceInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mContext: Context? =null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mContext = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(DeviceInfoViewModel::class.java)

        _binding = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDeviceInfo
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}