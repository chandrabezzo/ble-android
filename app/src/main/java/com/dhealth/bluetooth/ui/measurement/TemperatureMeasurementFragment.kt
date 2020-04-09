package com.dhealth.bluetooth.ui.measurement

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.bezzo.core.base.BaseFragment
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.TempMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import kotlinx.android.synthetic.main.fragment_temperature_measurement.*
import org.koin.android.ext.android.inject

class TemperatureMeasurementFragment : BaseFragment() {

    private val adapter: TempMeasurementRVAdapter by inject()
    private val viewModel: TemperatureViewModel by inject()

    override fun onViewInitialized(savedInstanceState: Bundle?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_temperature_measurement.adapter = adapter
        viewModel.getAll().observe(this, temperatures)

        sr_temperature_measurement.setOnRefreshListener {
            viewModel.getAll().observe(this, temperatures)
        }
    }

    override fun setLayout(): Int {
        return R.layout.fragment_temperature_measurement
    }

    private val temperatures = Observer<PagedList<Temperature>> {
        sr_temperature_measurement.isRefreshing = false
        adapter.submitList(it)
        adapter.notifyDataSetChanged()
    }
}
