package com.yushin.lockapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDeviceStatusDelegate
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import co.candyhouse.sesame.open.device.CHSesameBot
import com.yushin.lockapplication.R
import com.yushin.lockapplication.databinding.FragmentConnectLockBinding
import com.yushin.lockapplication.databinding.FragmentControlLockBinding
import com.yushin.lockapplication.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewModel.LockViewModel
import java.util.ArrayList

class ControlLockFragment : Fragment() {
    private var _binding: FragmentControlLockBinding? = null
    private val binding get() = _binding!!
    private lateinit var lockViewModel: LockViewModel
    private var connectedLock : LiveData<CHDevices>? = null
    private var selectedLock:CHDevices? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockViewModel = ViewModelProvider(requireActivity())[LockViewModel::class.java]
        connectedLock = lockViewModel.connectedLock
        Log.d("connectedLock",connectedLock.toString())
        selectedLock = connectedLock!!.value
        connectedLock?.observe(this){ lock ->
            selectedLock = lock
            selectedLock?.connect {  }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.setTitle(R.string.control_locks)
        _binding = FragmentControlLockBinding.inflate(inflater, container, false)

        var statusText = "Status:${selectedLock?.deviceStatus}"
        binding.itemStatus.text = statusText
        selectedLock?.delegate =  object : CHDeviceStatusDelegate {
            override fun onBleDeviceStatusChanged(
                device: CHDevices,
                status: CHDeviceStatus,
                shadowStatus: CHDeviceStatus?
            ) {
                super.onBleDeviceStatusChanged(device, status, shadowStatus)
                Log.d("onBleDeviceStatusChanged",status.toString())
                var statusText = "Status:$status"
                CoroutineScope(Main).launch {
                    if(_binding != null){
                        binding.itemStatus.text = statusText
                    }
                }
            }
        }

        //開錠処理
        binding.buttonUnlock.setOnClickListener(){
            Log.d("controlLock","starting unlock")
            selectedLock?.let { it1 -> controlLock(it1,1) }

        }

        //施錠処理
        binding.buttonLock.setOnClickListener(){
            Log.d("controlLock","starting lock")
            selectedLock?.let { it2 -> controlLock(it2,2) }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedLock?.disconnect {  }
        _binding = null
    }

    //1:unlock,2:lock
    private fun controlLock(device:CHDevices,value:Int){
        when (device) {
            is CHSesameBot -> {
                var checkBLELock = true
                CoroutineScope(IO).launch {
                    for (index in 0 until 5) {
                        if (checkBLELock) {
                            device.click {
                                it.onSuccess {
                                    checkBLELock = false
                                }
                            }
                            delay(2000)
                        }
                    }
                }
            }
            is CHSesame2 -> {//sesameOS2 ==> model--> sesame4  sesame2(客服認知sesame3)
                var checkBLELock = true// 記錄開鎖成功沒？
                CoroutineScope(IO).launch {
                    for (index in 0 until 5) {//每隔兩秒開一次開開
                        if (checkBLELock) {
                            when(value){
                                 1 ->{
                                     device.unlock {
                                         it.onSuccess {
                                             checkBLELock = false
                                             Log.d("controlLock","complete unlock")
                                         }
                                         it.onFailure {}
                                     }
                                }
                                2->{
                                    device.lock {
                                        it.onSuccess {
                                            checkBLELock = false
                                            Log.d("controlLock","complete lock")
                                        }
                                        it.onFailure {}
                                    }
                                }
                            }

                            delay(2000)
                        }
                    }
                }
            }
            is CHSesame5 -> {
                var checkBLELock = true// 記錄開鎖成功沒？
                CoroutineScope(IO).launch {
                    for (index in 0 until 5) {//每隔兩秒開一次開開
                        if (checkBLELock) {
                            when(value){
                                1 ->{
                                    device.unlock {
                                        it.onSuccess {
                                            checkBLELock = false
                                        }
                                        it.onFailure {}
                                    }
                                }
                                2->{
                                    device.lock {
                                        it.onSuccess {
                                            checkBLELock = false
                                        }
                                        it.onFailure {}
                                    }
                                }
                            }

                            delay(2000)
                        }
                    }
                }
            }
        }//end when (device)

    }
}