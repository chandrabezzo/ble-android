package com.dhealth.bluetooth.ui

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bezzo.core.base.BaseActivity
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.adapter.BleDataRVAdapter
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.BleUtil
import com.dhealth.bluetooth.util.measurement.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
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
import kotlinx.android.synthetic.main.activity_optical_hrm.*
import kotlinx.android.synthetic.main.activity_temperature.*
import kotlinx.android.synthetic.main.activity_temperature.toolbar
import org.koin.android.ext.android.inject
import kotlin.math.max

class OpticalHrmActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val compositeDisposable: CompositeDisposable by inject()
    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable
    private val ch1 =  LineDataSet(ArrayList<Entry>(), "ch1")
    private val ch2 =  LineDataSet(ArrayList<Entry>(), "ch2")

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

        chartDesign()
    }

    override fun setLayout(): Int {
        return R.layout.activity_optical_hrm
    }

    override fun onDestroy() {
        MeasurementUtil.commandStop(compositeDisposable, connection)
        connectionDisposable.dispose()
        super.onDestroy()
    }

    private fun doMeasurement(){
        HrmUtil.commandMinConfidenceLevel(compositeDisposable, connection, 0)
        HrmUtil.commandHrExpireDuration(compositeDisposable, connection, 30)
        HrmUtil.commandReadHrm(compositeDisposable, connection)
        HrmUtil.commandGetHrm(compositeDisposable, connection, object : HrmCallback{
            override fun originalData(values: ByteArray) {
                Log.i("Data HRM", values.contentToString())
            }

            override fun channel(channel1: Int, channel2: Int) {
                Log.i("Channel", "$channel1 & $channel2")
                runOnUiThread { renderDataSet(channel1.toFloat(), channel2.toFloat()) }
            }

            override fun heartRate(value: Int) {
                Log.i("Heart Rate", value.toString())
                if(value.toString() != tv_heart_rate.text){
                    runOnUiThread { tv_heart_rate.text = "$value ${getString(R.string.bpm)}" }
                }
            }

            override fun heartRateConfidence(value: String) {
                Log.i("Heart Rate Confidence", value)
                if(value != tv_confident.text){
                    runOnUiThread { tv_confident.text = value }
                }
            }

            override fun activity(value: String) {
                Log.i("Activity", value)
                if(value != tv_activity.text){
                    runOnUiThread { tv_activity.text = value }
                }
            }

        })
    }

    private fun chartDesign(){
        setupDataSet(ch1, R.color.colorPrimary, YAxis.AxisDependency.LEFT)
        setupDataSet(ch2, R.color.colorAccent, YAxis.AxisDependency.RIGHT)

        hrm_chart.setNoDataTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        hrm_chart.legend.isEnabled = true
        hrm_chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        hrm_chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        hrm_chart.legend.form = Legend.LegendForm.CIRCLE
        hrm_chart.legend.typeface = Typeface.create(hrm_chart.legend.typeface, Typeface.BOLD)
        hrm_chart.xAxis.isEnabled = false
        hrm_chart.xAxis.axisMinimum = 0F
        hrm_chart.xAxis.axisMaximum = 100F
        hrm_chart.axisLeft.isEnabled = false
        hrm_chart.axisRight.isEnabled = false
        hrm_chart.description.isEnabled = false
        hrm_chart.setTouchEnabled(false)
        hrm_chart.setVisibleXRangeMaximum(100F)

        doMeasurement()
    }

    private fun setupDataSet(dataSet: LineDataSet, @ColorRes color: Int, axisDependency: YAxis.AxisDependency){
        dataSet.color = ContextCompat.getColor(this, color)
        dataSet.lineWidth = 2F
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.axisDependency = axisDependency
    }

    private fun renderDataSet(value1: Float, value2: Float){
        if(hrm_chart.data == null) hrm_chart.data = LineData()

        if(hrm_chart.data.dataSets.isEmpty()){
            hrm_chart.data.addDataSet(ch1)
            hrm_chart.data.addDataSet(ch2)
        }

        val data = if(ch1.entryCount != 0){
            val entryForIndex = ch1.getEntryForIndex(ch1.entryCount - 1)
            entryForIndex.x + 1F
        } else {
            0F
        }

        addEntryToDataSet(ch1, Entry(data, value1))
        addEntryToDataSet(ch2, Entry(data, value2))

        adjustYAxisMinMax()

        if(data > 100){
            hrm_chart.xAxis.resetAxisMinimum()
            hrm_chart.xAxis.resetAxisMaximum()
        }

        hrm_chart.data.notifyDataChanged()
        hrm_chart.notifyDataSetChanged()
        hrm_chart.moveViewToX(data)
    }

    private fun adjustYAxisMinMax(){
        val yMax1 = ch1.yMax - ch1.yMin
        val yMax2 = ch2.yMax - ch2.yMin
        val max = max(1F, max(yMax1, yMax2)) * (1/0.6)
        val max1 = (max - yMax1)/2
        val max2 = (max - yMax2)/2
        hrm_chart.axisLeft.axisMinimum = (ch1.yMin - max1).toFloat()
        hrm_chart.axisLeft.axisMaximum = (ch1.yMax + max1).toFloat()
        hrm_chart.axisRight.axisMinimum = (ch2.yMin - max2).toFloat()
        hrm_chart.axisRight.axisMaximum = (ch2.yMax + max2).toFloat()
    }

    private fun addEntryToDataSet(dataSet: LineDataSet, data: Entry){
        if(dataSet.entryCount == 100) dataSet.removeFirst()
        dataSet.addEntry(data)
    }
}
