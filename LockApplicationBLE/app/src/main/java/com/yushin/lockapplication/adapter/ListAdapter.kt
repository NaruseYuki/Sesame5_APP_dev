package com.yushin.lockapplication.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yushin.lockapplication.R
import com.yushin.lockapplication.entities.LockEntity
import com.yushin.lockapplication.model.LockModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListAdapter(private val itemList: MutableList<LockEntity>, private val onItemClick: (LockEntity) -> Unit,) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.item_text)

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = itemList[position]
                    onItemClick(clickedItem)
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
        val currentItem = itemList[position]
        holder.itemText.text = currentItem.name
    }

    override fun getItemCount() = itemList.size

//    fun removeItem(position: Int) {
//        val currentItem = itemList[position]
//        itemList.removeAt(position)
//        val lockModel = LockModel.getInstance()
//        val coroutineScope = CoroutineScope(Dispatchers.IO)
//        coroutineScope.launch {
//            // 非同期処理を実行
//            val lockEntity = LockEntity(
//                currentItem.id,
//                currentItem.name,
//                currentItem.lockPosition,
//                currentItem.unlockPosition,
//                currentItem.apiKey
//            )
//            lockModel.deleteLock(lockEntity)
//        }
//        notifyItemRemoved(position)
//    }
}
