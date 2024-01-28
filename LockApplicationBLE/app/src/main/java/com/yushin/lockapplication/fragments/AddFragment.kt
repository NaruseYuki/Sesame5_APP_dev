package com.yushin.lockapplication.fragments

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yushin.lockapplication.R
import com.yushin.lockapplication.entities.LockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.yushin.lockapplication.constants.Constants
import viewModel.LockViewModel
import java.util.UUID

class AddFragment  : Fragment() {

    private var _binding: FragmentConnectLockBinding? = null
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
    ): View {
        _binding = FragmentConnectLockBinding.inflate(inflater, container, false)
        activity?.setTitle(R.string.add_setting)
        binding.editTextLockPosition.filters = arrayOf(InputFilterMinMax(Constants.MIN_POSITION, Constants.MAX_POSITION))
        binding.editTextUnlockPosition.filters = arrayOf(InputFilterMinMax(Constants.MIN_POSITION, Constants.MAX_POSITION))
        binding.scroll.setOnTouchListener { _, _ ->
            showOffKeyboard()
            false // タッチイベントを他のリスナーに渡すために false を返す
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
            startConnection()
            findNavController().navigate(R.id.action_addFragment_to_registeredLocksFragment)
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

    private fun startConnection() {
        //DB追加
        insertLock()
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

    private fun insertLock(){
        // CoroutineScopeを作成
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            val lockEntity = LockEntity( 0,
                binding.editTextLockName.text.toString(),
                binding.editTextLockPosition.text.toString().toInt(),
                binding.editTextUnlockPosition.text.toString().toInt(),
                lockViewModel.connectedLock.value?.deviceId!!
            )
            lockViewModel.insertLock(lockEntity)
            // データベース処理完了後にプログレスバーを非表示に
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), R.string.save_done, Toast.LENGTH_SHORT).show()
           }
        }
    }

    private fun showOffKeyboard() {
        binding.root.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

}