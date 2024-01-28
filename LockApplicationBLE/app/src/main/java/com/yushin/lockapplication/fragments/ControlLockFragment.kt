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
import co.candyhouse.sesame.open.device.CHDeviceStatus.*
import co.candyhouse.sesame.open.device.CHDeviceStatusDelegate
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import co.candyhouse.sesame.open.device.CHSesameBot
import com.yushin.lockapplication.R
import com.yushin.lockapplication.databinding.FragmentConnectLockBinding
import com.yushin.lockapplication.databinding.FragmentControlLockBinding
import com.yushin.lockapplication.databinding.FragmentFirstBinding
import com.yushin.lockapplication.entities.LockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewModel.LockViewModel
import java.util.ArrayList
import java.util.UUID

class ControlLockFragment : Fragment() {
    private var _binding: FragmentControlLockBinding? = null
    private val binding get() = _binding!!
    private lateinit var lockViewModel: LockViewModel
    private var connectedLock : LiveData<CHDevices>? = null
    private var connectedLockValue:CHDevices? = null
    private  var lockNameList:Map<UUID?,String>? = null
    private var lockList : LiveData<Map<UUID?,String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockViewModel = ViewModelProvider(requireActivity())[LockViewModel::class.java]
        connectedLock = lockViewModel.connectedLock
        Log.d("connectedLock",connectedLock.toString())
        lockList = lockViewModel.lockList
        lockNameList = lockList!!.value
        lockList?.observe(this){ it ->
            lockNameList = it
        }
        connectedLockValue = connectedLock!!.value
        connectedLock?.observe(this){
            //connectedLockValue = lock
            lockViewModel.connect()
            run lockName@{
                lockNameList?.forEach() {
                    if (it.key == connectedLockValue?.deviceId){
                        binding.itemLock.text = "ロック名："+ it.value
                        return@lockName
                    }
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.setTitle(R.string.control_locks)
        _binding = FragmentControlLockBinding.inflate(inflater, container, false)
        var statusText = "ロックの状態:"
        statusText += when(connectedLockValue?.deviceStatus){
            Locked -> "施錠"
            Unlocked -> "解錠"
            IotDisconnected -> "ロック切断"
            else -> "通信中"
        }

        binding.itemStatus.text = statusText
        connectedLockValue?.delegate =  object : CHDeviceStatusDelegate {
            override fun onBleDeviceStatusChanged(
                device: CHDevices,
                status: CHDeviceStatus,
                shadowStatus: CHDeviceStatus?
            ) {
                super.onBleDeviceStatusChanged(device, status, shadowStatus)
                Log.d("onBleDeviceStatusChanged",status.toString())
                var statusText = "ロックの状態:"
                CoroutineScope(Main).launch {
                    if(_binding != null){
                        statusText += when(status){
                            Locked -> "施錠"
                            Unlocked -> "解錠"
                            IotDisconnected -> "ロック切断"
                            else -> "通信中"
                        }
                        binding.itemStatus.text = statusText
                    }
                }
            }
        }

        //開錠処理
        binding.buttonUnlock.setOnClickListener(){
            Log.d("controlLock","starting unlock")
            connectedLockValue?.let { it1 -> controlLock(it1,1) }

        }

        //施錠処理
        binding.buttonLock.setOnClickListener(){
            Log.d("controlLock","starting lock")
            connectedLockValue?.let { it2 -> controlLock(it2,2) }
        }
        //設定変更
        binding.buttonOption.setOnClickListener(){
            //設定画面を呼び出す

        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        //lockViewModel.disconnect()
        _binding = null
    }

    //1:unlock,2:lock
    private fun controlLock(device:CHDevices,value:Int){
        lockViewModel.controlLock(device,value)
    }
}