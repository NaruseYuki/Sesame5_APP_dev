package com.yushin.lockapplication.fragments

import android.annotation.SuppressLint
import android.content.Context
import com.yushin.lockapplication.model.LockModel
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.yushin.lockapplication.databinding.FragmentConnectLockBinding
import android.text.InputFilter
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import co.candyhouse.sesame.open.device.CHSesame2
import com.yushin.lockapplication.R
import com.yushin.lockapplication.entities.LockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import viewModel.LockViewModel

class LockSettingFragment  : Fragment() {

    private var _binding: FragmentConnectLockBinding? = null
    private val binding get() = _binding!!
    private lateinit var lockModel:LockModel
    private var lockName:String? = null
    private var lockPosition:Int? = null
    private var unLockPosition:Int? = null
    private var lockEntity:LockEntity? = null
    private var registerDevFlg:Boolean? = false
    private lateinit var lockViewModel: LockViewModel
    private lateinit var selectedSetting: LiveData<LockEntity?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModelのインスタンスを取得
        lockViewModel = ViewModelProvider(requireActivity())[LockViewModel::class.java]

        // ロックエンティティを取得
        selectedSetting = lockViewModel.selectedLock
        lockEntity= selectedSetting.value
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectLockBinding.inflate(inflater, container, false)
        activity?.setTitle(R.string.lock_settings)
        lockModel = LockModel.getInstance()
        // 数値範囲の制限を追加
        binding.editTextLockPosition.filters = arrayOf(InputFilterMinMax("0", "1024"))
        binding.editTextUnlockPosition.filters = arrayOf(InputFilterMinMax("0", "1024"))
        binding.scroll.setOnTouchListener { _, _ ->
            showOffKeyboard()
            false // タッチイベントを他のリスナーに渡すために false を返す
        }
        if(lockEntity !=null){
            lockName = lockEntity!!.name
            lockPosition= lockEntity!!.lockPosition
            unLockPosition= lockEntity!!.unlockPosition

        }
        if(registerDevFlg == true){
            activity?.setTitle(R.string.setting_select)
            binding.buttonConnect.isVisible = true
            binding.buttonSave.isVisible = false
            binding.editTextLockName.isEnabled = false
            binding.editTextLockPosition.isEnabled = false
            binding.editTextUnlockPosition.isEnabled = false
        }else{
            activity?.setTitle(R.string.lock_settings)
            binding.buttonConnect.isVisible = false
            binding.buttonSave.isVisible = true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // EditText の入力状態を監視して Connect ボタンの操作可能性を更新
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateConnectButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editTextLockName.addTextChangedListener(textWatcher)
        binding.editTextLockPosition.addTextChangedListener(textWatcher)
        binding.editTextUnlockPosition.addTextChangedListener(textWatcher)

        // Connect ボタンの初期状態を更新
        updateConnectButtonState()

        binding.buttonSave.setOnClickListener {
            showOffKeyboard()
            updateSetting()
        }
        binding.buttonConnect.setOnClickListener {
            //現在接続中のデバイスに施錠開錠角度を設定する

        }
    }

    private fun updateConnectButtonState() {
        val editTextLockName = binding.editTextLockName
        val editTextLockPosition = binding.editTextLockPosition
        val editTextUnlockPosition = binding.editTextUnlockPosition
        val buttonSave = binding.buttonSave

        val isAllFieldsFilled = editTextLockName.text.isNotEmpty() &&
                editTextLockPosition.text.isNotEmpty() &&
                editTextUnlockPosition.text.isNotEmpty()
        buttonSave.isEnabled = isAllFieldsFilled
    }
    private fun updateSetting() {
        binding.progressBar.visibility = View.VISIBLE
        updateLock()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // 数値の範囲を制限するInputFilterの実装
    inner class InputFilterMinMax(private val min: String, private val max: String) : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toDouble()
                if (isInRange(input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                // 無効な入力
            }
            return ""
        }

        private fun isInRange(input: Double): Boolean {
            return input in min.toDouble()..max.toDouble()
        }
    }

    private fun updateLock(){
        val lockEntity = LockEntity(
            lockEntity!!.id,//変更しない
            binding.editTextLockName.text.toString(),
            binding.editTextLockPosition.text.toString().toInt(),
            binding.editTextUnlockPosition.text.toString().toInt(),
            lockEntity!!.uuid//UUIDは変更しない
        )
        val coroutineScope =CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            // 非同期処理を実行
            lockModel.updateLock(lockEntity)
            // データベース処理完了後にメインスレッドでUI更新
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), R.string.save_done, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.editTextLockName.setText(lockName)
        binding.editTextLockPosition.setText(lockPosition.toString())
        binding.editTextUnlockPosition.setText(unLockPosition.toString())
    }
    // キーボード非表示
    private fun showOffKeyboard() {
        binding.root.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}