package com.dhealth.personalasssesment.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bezzo.core.base.BaseHolder
import com.bezzo.core.listener.OnItemClickListener
import com.dhealth.personalasssesment.R
import kotlinx.android.synthetic.main.item_rv_device.view.*

class BluetoothDeviceRVAdapter(private val list: MutableList<BluetoothDevice>)
    : RecyclerView.Adapter<BluetoothDeviceRVAdapter.Item>() {

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

    fun getItem(position: Int): BluetoothDevice {
        return list[position]
    }

    fun setItems(values: MutableList<BluetoothDevice>){
        list.clear()
        list.addAll(values)
    }

    fun addItem(value: BluetoothDevice){
        if(!list.contains(value)) list.add(value)
    }

    inner class Item(itemView: View): BaseHolder<BluetoothDevice>(itemView){

        init {
            itemView.setOnClickListener { listener.onItemClick(it, layoutPosition) }
        }

        override fun setContent(model: BluetoothDevice) {
            itemView.tv_name.text = model.name
            itemView.tv_address.text = model.address
        }
    }
}