package com.yushin.lockapplication.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.lockapplication.R
import com.yushin.lockapplication.entities.LockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import viewModel.LockViewModel
import java.util.UUID


class GenericListAdapter<T>(private val itemList: MutableList<Any>, private val onItemClick:(Any)->Unit, private val nameList: Map<UUID?, String>?
,private val lockViewModel: LockViewModel) :
    RecyclerView.Adapter<GenericListAdapter<T>.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)
        init {
            // ViewModelのインスタンスを取得
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                   val item = itemList[position]
                    onItemClick(item)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(val currentItem = itemList[position]){
            is CHDevices -> {
                //登録済みデバイス画面のみ、ロック名を表示させたい
                if(nameList?.get(currentItem.deviceId).isNullOrEmpty()){
                    holder.itemText.text = currentItem.deviceId.toString()
                }else{
                    holder.itemText.text = nameList?.get(currentItem.deviceId)
                }
            }
            is LockEntity ->{
                holder.itemText.text = currentItem.name
            }
        }
    }

    override fun getItemCount() = itemList.size

    fun removeItem(position: Int) {
        val currentItem = itemList[position]
        itemList.removeAt(position)
        when( currentItem){
            is LockEntity ->{
                val coroutineScope = CoroutineScope(Dispatchers.IO)
                coroutineScope.launch {
                    // 非同期処理を実行
                    lockViewModel.deleteLock(currentItem)
                }
            }
            is CHDevices ->{
                currentItem.dropKey {  }
            }
        }
        notifyItemRemoved(position)
    }
}

