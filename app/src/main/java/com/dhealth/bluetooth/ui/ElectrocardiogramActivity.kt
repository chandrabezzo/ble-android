package com.dhealth.bluetooth.ui

import android.icu.util.Measure
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.measurement.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_electrocardiogram.*
import org.koin.android.ext.android.inject

class ElectrocardiogramActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val compositeDisposable: CompositeDisposable by inject()
    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable
    private val movingAverage = MovingAverage(5)
    private val lineDataset =  LineDataSet(ArrayList<Entry>(), "Data ECG")

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        setSupportActionBar(toolbar)

        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

        movingAverage.reset()
        chartDesign()
    }

    override fun setLayout(): Int {
        return R.layout.activity_electrocardiogram
    }

    override fun onDestroy() {
        MeasurementUtil.commandStop(compositeDisposable, connection)
        connectionDisposable.dispose()
        super.onDestroy()
    }

    private fun doMeasurement(isDefault: Boolean){
        if (isDefault){
            EcgUtil.commandSendDefaultRegisterValues(compositeDisposable, connection)
        }
        else {
            EcgUtil.commandCreateGetRegister(compositeDisposable, connection)
        }

        EcgUtil.commandReadEcg(compositeDisposable, connection)
        EcgUtil.commandGetEcg(compositeDisposable, connection, isDefault, movingAverage,
            object : EcgCallback {
                override fun originalData(values: ByteArray) {
                    Log.i("Data ECG", values.contentToString())
                }

                override fun ecgMv(value: Float) {
                    Log.i("ECG", MeasurementUtil.decimalFormat(value))
                    runOnUiThread {
                        renderDataSet(value);
                        tv_ecg_mv.text = value.toString()
                    }
                }

                override fun averageRToR(value: Float) {
                    Log.i("Average R-to-R", MeasurementUtil.decimalFormat(value))
                    runOnUiThread { tv_average.text = "${MeasurementUtil.decimalFormat(value)} " +
                            "${getString(R.string.bpm)}" }
                }

                override fun currentRToR(value: Int) {
                    runOnUiThread { tv_current.text = "$value ${getString(R.string.bpm)}" }
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ecg_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_insert -> {
                toast("Invert ECG")
            }
        }
        return true
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
        ecg_chart.xAxis.axisMaximum = 512F
        ecg_chart.setVisibleXRangeMaximum(512F)

        doMeasurement(false)
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

        if(xValue > 512){
            ecg_chart.xAxis.resetAxisMinimum()
            ecg_chart.xAxis.resetAxisMaximum()
        }

        ecg_chart.data.notifyDataChanged()
        ecg_chart.notifyDataSetChanged()
        ecg_chart.moveViewToX(xValue)
    }

    private fun addEntryToDataSet(data: Entry){
        if(lineDataset.entryCount == 512) lineDataset.removeFirst()

        lineDataset.addEntry(data)
    }
}
