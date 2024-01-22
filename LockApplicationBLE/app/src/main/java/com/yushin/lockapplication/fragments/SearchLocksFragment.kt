package com.yushin.lockapplication.fragments

import com.yushin.lockapplication.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHBleManagerDelegate
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDeviceStatusDelegate
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.lockapplication.adapter.GenericListAdapter
import com.yushin.lockapplication.databinding.FragmentFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import viewModel.LockViewModel
import java.util.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SearchLocksFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    var mDeviceList = ArrayList<CHDevices>()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var lockViewModel: LockViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModelのインスタンスを取得
        lockViewModel = ViewModelProvider(requireActivity())[LockViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        activity?.setTitle(R.string.search_locks)
        val view = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = GenericListAdapter<CHDevices>(mDeviceList as MutableList<Any>, { chDevice ->
            when(chDevice){
                is CHDevices ->{
                    chDevice.connect{
                    }
                    lockViewModel.setConnectedLock(chDevice)

                    //ここでは選択したロックをセットするだけにしておく
                    //登録はDBに設定情報を登録してから行う
                    //Todo
                    chDevice.delegate =  object : CHDeviceStatusDelegate {
                       override fun onBleDeviceStatusChanged(
                           device: CHDevices,
                           status: CHDeviceStatus,
                           shadowStatus: CHDeviceStatus?
                       ) {
                           super.onBleDeviceStatusChanged(device, status, shadowStatus)
                               if (status == CHDeviceStatus.ReadyToRegister) {
                                   doRegisterDevice(chDevice) }
                       }
                    }
                    Toast.makeText(requireContext(), "Clicked: ${chDevice.deviceId}", Toast.LENGTH_SHORT).show()
                }
            }
            //ロックの名前を次のフラグメントに引き継ぐ
            findNavController().navigate(R.id.action_searchLocksFragment_to_addFragment)
        }, null)
            binding.recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverUnRegisteredCHDevices(devices: List<CHDevices>) {
                mDeviceList.clear()
                mDeviceList.addAll(
                    devices.filter { it.rssi != null }
                )
                mDeviceList.firstOrNull()?.connect { }
                CoroutineScope(Dispatchers.Main).launch {
                    if(_binding != null){
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun doRegisterDevice(device: CHDevices){
        device.register {
            it.onSuccess {
                //  登録成功
                Log.d("doRegisterDevice","登録成功")
                //初期値の角度を登録しておく
                (device as? CHSesame2)?.let {
                    device.configureLockPosition(0, 256) {}//1024 --> 360,256-->90
                    device.setHistoryTag("Test1".toByteArray()) {}
                    Log.d("doRegisterDevice","CHSesame2")
                }
                (device as? CHSesame5)?.let {
                    device.configureLockPosition(0, 256) {}//1024 --> 360,256-->90
                    device.setHistoryTag("Test2".toByteArray()) {}
                    Log.d("doRegisterDevice","CHSesame5")
                }
            }
            it.onFailure {
                //  登録失敗
                Log.d("doRegisterDevice","登録失敗")
            }
        }
    }
}