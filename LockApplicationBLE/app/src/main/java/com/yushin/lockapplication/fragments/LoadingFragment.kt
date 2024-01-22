package com.yushin.lockapplication.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yushin.lockapplication.R

class LoadingFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 5秒待つためのハンドラを作成
        handler.postDelayed({
           // findNavController().navigate(R.id.action_loadingFragment_to_addFragment)
        }, 5000) // 5000ミリ秒 = 5秒

    }
    override fun onDestroy() {
        super.onDestroy()
        // ハンドラに対して待機中の処理をキャンセル
        handler.removeCallbacksAndMessages(null)
    }

}