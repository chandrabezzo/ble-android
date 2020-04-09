package com.dhealth.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bezzo.core.base.BaseHolder
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import kotlinx.android.synthetic.main.item_rv_hrm_measurement.view.*

class HrmMeasurementRVAdapter: PagedListAdapter<Hrm, HrmMeasurementRVAdapter.Item>(callback) {

    companion object {
        val callback = object : DiffUtil.ItemCallback<Hrm>(){
            override fun areItemsTheSame(oldItem: Hrm, newItem: Hrm): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Hrm, newItem: Hrm): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        return Item(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_hrm_measurement,
            parent, false))
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.model = getItem(position)
    }

    inner class Item(itemView: View): BaseHolder<Hrm>(itemView){
        override fun setContent(model: Hrm) {
            itemView.tv_date.text = MeasurementUtil.getDateTime(model.id)
            itemView.tv_heart_rate.text = "${model.heartRate} ${itemView.context.getString(R.string.bpm)}"
            itemView.tv_activity.text = "${model.activity} | Confidence: ${model.confidence}"
        }
    }
}