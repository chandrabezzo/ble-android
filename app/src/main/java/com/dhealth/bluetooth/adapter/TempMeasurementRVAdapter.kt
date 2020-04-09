package com.dhealth.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bezzo.core.base.BaseHolder
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import kotlinx.android.synthetic.main.item_rv_temp_measurement.view.*

class TempMeasurementRVAdapter: PagedListAdapter<Temperature, TempMeasurementRVAdapter.Item>(callback) {

    companion object {
        val callback = object : DiffUtil.ItemCallback<Temperature>(){
            override fun areItemsTheSame(oldItem: Temperature, newItem: Temperature): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Temperature, newItem: Temperature): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        return Item(LayoutInflater.from(parent.context).inflate(R.layout.item_rv_temp_measurement,
            parent, false))
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.model = getItem(position)
    }

    inner class Item(itemView: View): BaseHolder<Temperature>(itemView){
        override fun setContent(model: Temperature) {
            itemView.tv_date.text = MeasurementUtil.getDateTime(model.id)
            val celcius = "Celcius: ${MeasurementUtil.decimalFormat(model.inCelcius)} " +
                    "${itemView.context.getString(R.string.derajat_celcius)}"
            val fahrenheit = "Fahrenheit: ${MeasurementUtil.decimalFormat(model.inFahrenheit)} " +
                    "${itemView.context.getString(R.string.derajat_fahrenheit)}"
            itemView.tv_temperature.text = "$celcius | $fahrenheit"
        }
    }
}