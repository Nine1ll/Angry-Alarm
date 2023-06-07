package kr.nine1ll.newtext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.nine1ll.newtext.databinding.TextListBinding
import kr.nine1ll.newtext.textdb.TextTable

class TextAdapter(private var dataSet: MutableList<TextTable>) : RecyclerView.Adapter<TextAdapter.TextViewHolder>() {

    class TextViewHolder(val binding: TextListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val binding = TextListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TextViewHolder(binding)
    }

    fun setList(newList: MutableList<TextTable>) {
        this.dataSet = newList
    }

    fun getElement(pos: Int): TextTable {
        return dataSet[pos]
    }

    private lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemLongClickListener: OnItemLongClickEventListener

    interface OnItemLongClickEventListener {
        fun onItemLongClick(v: View, position: Int)
    }

    fun setOnItemLongClickListener(onItemLongClickEventListener: OnItemLongClickEventListener){
        itemLongClickListener = onItemLongClickEventListener
    }


    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val binding = holder.binding

        binding.messageText.text = dataSet[position].message
        binding.phoneNum.text = dataSet[position].phoneNumber

        binding.textItem.setOnClickListener {
            itemClickListener.onClick(it, position)
        }

        binding.textItem.setOnLongClickListener {
            itemLongClickListener.onItemLongClick(it, position)
            true
        }
    }
}
