package com.dhealth.bluetooth.ui.measurement

import android.app.Dialog
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
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.TempMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.ui.Progress
import com.dhealth.bluetooth.util.Prepare
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.util.measurement.TemperatureUtil
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import com.parse.ParseObject
import kotlinx.android.synthetic.main.fragment_temperature_measurement.*
import org.koin.android.ext.android.inject

class TemperatureMeasurementFragment : BaseFragment() {

    private val adapter: TempMeasurementRVAdapter by inject()
    private val viewModel: TemperatureViewModel by inject()
    private lateinit var syncDialog: Dialog

    override fun onViewInitialized(savedInstanceState: Bundle?) {
        context?.let { context ->
            syncDialog = Progress.dialog(context, getString(R.string.sync_data))
        }
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
        inflater.inflate(R.menu.measurement_result_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_share -> {
                context?.let { context ->
                    viewModel.share(context).observe(this, sharingMeasurement)
                }
            }
            R.id.nav_sync -> {
                syncDialog.show()
                viewModel.sync().observe(this, syncTemperature)
            }
        }

        return true
    }

    private val sharingMeasurement = Observer<ShareState> { state ->
        context?.let { context ->
            val progressDialog = Progress.dialog(context, "Mempersiapkan Data...")
            when(state){
                Prepare -> progressDialog.show()
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

    private val syncTemperature = Observer<MutableList<Temperature>> {
        for(temperature in it){
            val parseObject = TemperatureUtil.temperatureToParseObject(temperature)
            parseObject.saveInBackground()
            viewModel.synced(temperature)
        }
        syncDialog.dismiss()
    }
}
