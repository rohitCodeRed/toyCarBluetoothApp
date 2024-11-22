package com.example.toycarbluetoothapp.ui.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.toycarbluetoothapp.Constants
import com.example.toycarbluetoothapp.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID

class ClassicUpdateUuid(context:Context): BottomSheetDialogFragment(), OnClickListener {
    var mContext:Context = context
    lateinit var  v:View
    private lateinit var descrpInput: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        v = inflater.inflate(R.layout.bottom_sheet_classic_uuid, container, false)

        val updateBtn: Button = v.findViewById<Button>(R.id.classic_update)
        val cancelBtn: Button = v.findViewById(R.id.classic_cancel)

        updateBtn.setOnClickListener(this)
        cancelBtn.setOnClickListener(this)

        descrpInput = v.findViewById(R.id.classic_descpt_input)
        descrpInput.editText?.setText(Constants.CLIENT_UUID)

        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // used to show the bottom sheet dialog
        dialog?.setOnShowListener { it ->
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }

    companion object {
        const val TAG = "ModalBottomSheetClassicUUID"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.classic_update ->{
                checkAndUpdateInputsVal()
                this.dismiss()
            }
            R.id.classic_cancel ->{
                this.dismiss()
            }
        }
    }

    private fun checkAndUpdateInputsVal(){
        if(descrpInput.editText?.text != null){
            try {
                var uid = UUID.fromString(descrpInput.editText?.text.toString())
                Constants.updateClassicUuid(descrpInput.editText?.text.toString())
                Toast.makeText(mContext,
                    "UUID got updated...",
                    Toast.LENGTH_SHORT).show();
            }catch(e:Exception){
                Toast.makeText(mContext,
                    "Invalid UUID.!! error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT).show();
            }
            return

        }

        Toast.makeText(mContext,
            "UUID field should not be null.., please check",
            Toast.LENGTH_SHORT).show();
    }

}