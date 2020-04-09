package com.dhealth.bluetooth.ui.measurement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.bezzo.core.base.BaseFragment

import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.EcgMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import kotlinx.android.synthetic.main.fragment_ecg_measurement.*
import org.koin.android.ext.android.inject

class EcgMeasurementFragment : BaseFragment() {

    private val adapter: EcgMeasurementRVAdapter by inject()
    private val viewModel: EcgViewModel by inject()

    override fun onViewInitialized(savedInstanceState: Bundle?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_ecg_measurement.adapter = adapter
        viewModel.getAll().observe(this, ecgs)

        sr_ecg_measurement.setOnRefreshListener {
            viewModel.getAll().observe(this, ecgs)
        }
    }

    override fun setLayout(): Int {
        return R.layout.fragment_ecg_measurement
    }

    private val ecgs = Observer<PagedList<Ecg>> {
        sr_ecg_measurement.isRefreshing = false
        adapter.submitList(it)
        adapter.notifyDataSetChanged()
    }
}
