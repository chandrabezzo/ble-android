package com.dhealth.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bezzo.core.base.BaseHolder
import com.bezzo.core.listener.OnItemClickListener
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.model.BleDevice
import kotlinx.android.synthetic.main.item_rv_device.view.*

class BleDeviceRVAdapter(private val list: MutableList<BleDevice>)
    : RecyclerView.Adapter<BleDeviceRVAdapter.Item>() {

    lateinit var listener: OnItemClickListener

    fun setOnItemClick(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        return Item(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_device, parent, false))
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.model = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItems(): MutableList<BleDevice> {
        return list
    }

    fun getItem(position: Int): BleDevice {
        return list[position]
    }

    fun setItems(values: MutableList<BleDevice>){
        list.clear()
        list.addAll(values)
    }

    fun addItem(value: BleDevice){
        if(!list.contains(value)) list.add(value)
    }

    inner class Item(itemView: View): BaseHolder<BleDevice>(itemView){

        init {
            itemView.setOnClickListener { listener.onItemClick(it, layoutPosition) }
        }

        override fun setContent(model: BleDevice) {
            itemView.tv_name.text = model.device.name
            itemView.tv_address.text = model.device.address
        }
    }
}