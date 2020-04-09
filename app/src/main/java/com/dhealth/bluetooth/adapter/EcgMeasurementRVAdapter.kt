package com.dhealth.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bezzo.core.base.BaseHolder
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import kotlinx.android.synthetic.main.item_rv_ecg_measurement.view.*

class EcgMeasurementRVAdapter: PagedListAdapter<Ecg, EcgMeasurementRVAdapter.Item>(callback) {

    companion object {
        val callback = object : DiffUtil.ItemCallback<Ecg>(){
            override fun areItemsTheSame(oldItem: Ecg, newItem: Ecg): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Ecg, newItem: Ecg): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        return Item(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_ecg_measurement,
            parent, false))
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.model = getItem(position)
    }

    inner class Item(itemView: View): BaseHolder<Ecg>(itemView){
        override fun setContent(model: Ecg) {
            itemView.tv_date.text = MeasurementUtil.getDateTime(model.id)
            itemView.tv_heart_rate.text = model.ecgMv.toString()
            itemView.tv_average.text = "Average: ${model.averageRToRBpm}"
        }
    }
}