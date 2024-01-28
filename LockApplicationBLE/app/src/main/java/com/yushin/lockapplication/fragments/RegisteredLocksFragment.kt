package com.yushin.lockapplication.fragments

import com.yushin.lockapplication.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import co.candyhouse.sesame.open.CHDeviceManager
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.lockapplication.adapter.GenericListAdapter
import com.yushin.lockapplication.adapter.SwipeToDeleteCallback
import com.yushin.lockapplication.databinding.FragmentFirstBinding
import com.yushin.lockapplication.databinding.FragmentFirstBinding.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import viewModel.LockViewModel
import java.util.UUID

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RegisteredLocksFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var lockViewModel: LockViewModel
    private val binding get() = _binding!!
    private  var lockNameList:Map<UUID?,String>? = null
    private var lockList : LiveData<Map<UUID?,String>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockViewModel = ViewModelProvider(requireActivity())[LockViewModel::class.java]
        lockList = lockViewModel.lockList
        lockNameList = lockList!!.value
        lockList?.observe(this){ it ->
            lockNameList = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate(inflater, container, false)
            // ツールバーのタイトルを設定
            this.activity?.setTitle(R.string.registered_locks)
            val view = binding.root
            //鍵のリスト生成
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            CHDeviceManager.getCandyDevices {
                it.onSuccess {
                    val coroutineScopeMain = CoroutineScope(Dispatchers.Main)
                    coroutineScopeMain.launch{
                        val adapter = GenericListAdapter<CHDevices>(it.data as MutableList<Any>, { chDevice ->
                            when(chDevice){
                                is CHDevices ->{
                                    lockViewModel.setConnectedLock(chDevice)
                                    lockViewModel.connect()
                                    Toast.makeText(requireContext(), "Clicked: ${chDevice.deviceId}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            findNavController().navigate(R.id.action_registeredLocksFragment_to_controlLockFragment)
                        }, lockNameList,lockViewModel)
                        binding.recyclerView.adapter = adapter
                        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
                        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                    }
                }
            }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}