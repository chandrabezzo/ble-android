package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchActivityClearAllStack
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.util.measurement.*
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_electrocardiogram.*
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess

class ElectrocardiogramActivity : BaseActivity() {

    private val compositeDisposable: CompositeDisposable by inject()
    private val viewModel: EcgViewModel by inject()
    private val measurementVM: MeasurementViewModel by inject()
    private val bleClient: RxBleClient by inject()

    private lateinit var connection: Observable<RxBleConnection>
    private val movingAverage = MovingAverage(5)
    private val lineDataset =  LineDataSet(ArrayList<Entry>(), "Data ECG")
    private var isPlay = false
    private val max = 512F

    override fun onInitializedView(savedInstanceState: Bundle?) {
        connection = bleClient.getBleDevice(measurementVM.selectedDevice())
            .establishConnection(false).compose(ReplayingShare.instance())

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setSupportActionBar(toolbar)
        tv_device_info.text = measurementVM.selectedDevice()
        toolbar.title = "Electrocardiogram"

        chartDesign()
    }

    override fun setLayout(): Int {
        return R.layout.activity_electrocardiogram
    }

    private fun doMeasurement(isDefault: Boolean){
        if (isDefault){
            for(entry in Ecg.defaults.entries){
                val key = entry.key
                val value = entry.value

                compositeDisposable.add(EcgUtil.commandSendDefaultRegisterValues(connection, key, value))
            }
        }
        else {
            compositeDisposable.add(EcgUtil.commandCreateGetRegister(connection))
        }

        compositeDisposable.add(EcgUtil.commandReadEcg(connection))
        compositeDisposable.add(EcgUtil.commandGetEcg(connection, isDefault, movingAverage,
            object : EcgCallback {
                override fun originalData(values: ByteArray) {
                    Log.i("Data ECG", values.contentToString())
                }

                override fun ecgMonitor(
                    id: Long,
                    ecg: Int,
                    eTag: Int,
                    pTag: Int,
                    rTor: Int,
                    currentRToRBpm: Int,
                    ecgMv: Float,
                    filteredEcg: Float,
                    averageRToRBpm: Float,
                    counterToReport: Float
                ) {
                    Log.i("ECG", MeasurementUtil.decimalFormat(ecgMv))
                    runOnUiThread {
                        renderDataSet(ecgMv)
                        tv_ecg_mv.text = ecgMv.toString()
                    }

                    Log.i("Average R-to-R", MeasurementUtil.decimalFormat(averageRToRBpm))
                    runOnUiThread { tv_average.text = "${MeasurementUtil.decimalFormat(averageRToRBpm)} " +
                            "${getString(R.string.bpm)}" }

                    runOnUiThread { tv_current.text = "$currentRToRBpm ${getString(R.string.bpm)}" }

                    viewModel.add(Ecg(
                        ecg, eTag, pTag, rTor, currentRToRBpm, ecgMv, filteredEcg, averageRToRBpm,
                        counterToReport, MeasurementUtil.getEpoch()
                    ))
                }
            }))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ecg_menu, menu)
        val startMenu = menu?.findItem(R.id.nav_start)
        val stopMenu = menu?.findItem(R.id.nav_stop)

        if(isPlay) {
            startMenu?.isVisible = false
            stopMenu?.isVisible = true
            invalidateOptionsMenu()
        }
        else {
            startMenu?.isVisible = true
            stopMenu?.isVisible = false
            invalidateOptionsMenu()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_start -> {
                isPlay = true
                movingAverage.reset()
                clearData()

                EcgUtil.commandLogToFlash(connection)
                doMeasurement(false)
            }
            R.id.nav_stop -> {
                isPlay = false
                stopEcg()
            }
            R.id.nav_invert -> {
                toast("Invert")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun chartDesign(){
        lineDataset.color = ContextCompat.getColor(this, R.color.black_effective)
        lineDataset.lineWidth = 2F
        lineDataset.setDrawCircles(false)
        lineDataset.setDrawValues(false)
        ecg_chart.setNoDataTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        ecg_chart.legend.isEnabled = true
        ecg_chart.description.isEnabled = false
        ecg_chart.xAxis.isEnabled = false
        ecg_chart.axisRight.isEnabled = false
        ecg_chart.axisLeft.isEnabled = true
        ecg_chart.axisLeft.setDrawTopYLabelEntry(true)
        ecg_chart.setTouchEnabled(false)
        ecg_chart.isAutoScaleMinMaxEnabled = true
        ecg_chart.xAxis.axisMinimum = 0F
        ecg_chart.xAxis.axisMaximum = max
        ecg_chart.setVisibleXRangeMaximum(max)
    }

    private fun renderDataSet(value: Float){
        if(ecg_chart.data == null) ecg_chart.data = LineData()

        if(ecg_chart.data.dataSets.isEmpty()) ecg_chart.data.addDataSet(lineDataset)

        val xValue = if(lineDataset.entryCount != 0){
            val data = lineDataset.getEntryForIndex(lineDataset.entryCount - 1)
            data.x + 1F
        } else {
            0F
        }

        addEntryToDataSet(Entry(xValue, value))

        if(xValue > max){
            ecg_chart.xAxis.resetAxisMinimum()
            ecg_chart.xAxis.resetAxisMaximum()
        }

        ecg_chart.data.notifyDataChanged()
        ecg_chart.notifyDataSetChanged()
        ecg_chart.moveViewToX(xValue)
    }

    private fun addEntryToDataSet(data: Entry){
        if(lineDataset.entryCount == max.toInt()) lineDataset.removeFirst()

        lineDataset.addEntry(data)
    }

    private fun clearData(){
        if(ecg_chart.data != null){
            ecg_chart.data.removeDataSet(lineDataset)
            lineDataset.clear()
            ecg_chart.xAxis.axisMinimum = 0F
            ecg_chart.xAxis.axisMaximum = max
            ecg_chart.data = null
            ecg_chart.clear()
            ecg_chart.invalidate()

        }
    }

    private fun stopEcg(){
        MeasurementUtil.commandStop(connection)
        compositeDisposable.clear()
    }

    override fun onBackPressed() {
        stopEcg()
        compositeDisposable.dispose()
        exitProcess(0)
        launchActivityClearAllStack<ScanActivity>()
    }
}
