package com.dhealth.bluetooth.ui.measurement

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.bezzo.core.base.BaseFragment
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.TempMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.ui.Progress
import com.dhealth.bluetooth.util.Loading
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import kotlinx.android.synthetic.main.fragment_temperature_measurement.*
import org.koin.android.ext.android.inject

class TemperatureMeasurementFragment : BaseFragment() {

    private val adapter: TempMeasurementRVAdapter by inject()
    private val viewModel: TemperatureViewModel by inject()

    override fun onViewInitialized(savedInstanceState: Bundle?) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        activity?.actionBar?.title = getString(R.string.temperature)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_share -> {
                context?.let { context ->
                    viewModel.share(context).observe(this, sharingMeasurement)
                }
            }
        }

        return true
    }

    private val sharingMeasurement = Observer<ShareState> { state ->
        context?.let { context ->
            val progressDialog = Progress.dialog(context, "Mempersiapkan Data...")
            when(state){
                Loading -> progressDialog.show()
                Saved -> {
                    progressDialog.dismiss()
                    val tempFile = FileProvider.getUriForFile(context,
                        "com.dhealth.bluetooth.fileprovider", StorageUtil.readTemperature(context))
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "*/*"
                    intent.data = tempFile
                    intent.putExtra(Intent.EXTRA_STREAM, tempFile)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }
}
