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
import com.dhealth.bluetooth.adapter.HrmMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import kotlinx.android.synthetic.main.fragment_hrm_measurement.*
import org.koin.android.ext.android.inject

class HrmMeasurementFragment : BaseFragment() {

    private val adapter: HrmMeasurementRVAdapter by inject()
    private val viewModel: HrmViewModel by inject()

    override fun onViewInitialized(savedInstanceState: Bundle?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_hrm_measurement.adapter = adapter
        viewModel.getAll().observe(this, hrms)

        sr_hrm_measurement.setOnRefreshListener {
            viewModel.getAll().observe(this, hrms)
        }
    }

    override fun setLayout(): Int {
        return R.layout.fragment_hrm_measurement
    }

    private val hrms = Observer<PagedList<Hrm>> {
        sr_hrm_measurement.isRefreshing = false
        adapter.submitList(it)
        adapter.notifyDataSetChanged()
    }
}
