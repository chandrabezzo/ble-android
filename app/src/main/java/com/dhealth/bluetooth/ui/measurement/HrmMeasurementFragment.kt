package com.dhealth.bluetooth.ui.measurement

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.bezzo.core.base.BaseFragment

import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.HrmMeasurementRVAdapter
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.ui.Progress
import com.dhealth.bluetooth.util.Prepare
import com.dhealth.bluetooth.util.Saved
import com.dhealth.bluetooth.util.ShareState
import com.dhealth.bluetooth.util.StorageUtil
import com.dhealth.bluetooth.util.measurement.HrmUtil
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import kotlinx.android.synthetic.main.fragment_hrm_measurement.*
import org.koin.android.ext.android.inject

class HrmMeasurementFragment : BaseFragment() {

    private val adapter: HrmMeasurementRVAdapter by inject()
    private val viewModel: HrmViewModel by inject()
    private lateinit var syncDialog: Dialog

    override fun onViewInitialized(savedInstanceState: Bundle?) {
        context?.let { context ->
            syncDialog = Progress.dialog(context, getString(R.string.sync_data))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        activity?.actionBar?.title = getString(R.string.heart_rate)

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
                viewModel.sync().observe(this, syncHrm)
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
                    val hrmFile = FileProvider.getUriForFile(context,
                        "com.dhealth.bluetooth.fileprovider", StorageUtil.readHrm(context))
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "*/*"
                    intent.data = hrmFile
                    intent.putExtra(Intent.EXTRA_STREAM, hrmFile)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }

    private val syncHrm = Observer<MutableList<Hrm>> {
        for(hrm in it){
            val parseObject = HrmUtil.hrmToParseObject(hrm)
            parseObject.saveInBackground()
            viewModel.synced(hrm)
        }
        syncDialog.dismiss()
    }
}
