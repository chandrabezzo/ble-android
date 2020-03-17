package com.dhealth.bluetooth.adapter

import android.bluetooth.le.ScanResult
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bezzo.core.base.BaseHolder
import com.bezzo.core.listener.OnItemClickListener
import com.dhealth.bluetooth.R
import kotlinx.android.synthetic.main.item_rv_device.view.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScanResultRVAdapter(private val list: MutableList<ScanResult>)
    : RecyclerView.Adapter<ScanResultRVAdapter.Item>() {

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

    fun getItem(position: Int): ScanResult {
        return list[position]
    }

    fun getItems(): MutableList<ScanResult> {
        return list
    }

    fun setItems(values: MutableList<ScanResult>){
        list.clear()
        list.addAll(values)
    }

    fun addItem(value: ScanResult){
        var foundDevice = false
        for(result in list){
            if(result.device.address == value.device.address){
                foundDevice = true
                break
            }
        }
        if(!foundDevice) list.add(value)
    }

    inner class Item(itemView: View): BaseHolder<ScanResult>(itemView){
        init {
            itemView.setOnClickListener { listener.onItemClick(it, layoutPosition) }
        }

        override fun setContent(model: ScanResult) {
            itemView.tv_name.text = model.device.name
            itemView.tv_address.text = model.device.address
        }
    }
}