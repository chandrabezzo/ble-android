package com.dhealth.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bezzo.core.base.BaseHolder
import com.dhealth.bluetooth.R
import kotlinx.android.synthetic.main.item_rv_device_data.view.*

class BleDataRVAdapter(private val list: MutableList<String>)
    : RecyclerView.Adapter<BleDataRVAdapter.Item>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        return Item(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_device_data,
            parent, false))
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.model = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addData(value: String){
        list.add(value)
    }

    inner class Item(itemView: View): BaseHolder<String>(itemView){
        override fun setContent(model: String) {
            itemView.tv_data.text = model
        }
    }
}