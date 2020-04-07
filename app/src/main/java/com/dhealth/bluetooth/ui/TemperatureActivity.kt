package com.dhealth.bluetooth.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.toast
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.Extras
import com.dhealth.bluetooth.data.model.BleDevice
import com.dhealth.bluetooth.util.TemperatureValueFormatter
import com.dhealth.bluetooth.util.measurement.MeasurementUtil
import com.dhealth.bluetooth.util.measurement.RxBus
import com.dhealth.bluetooth.util.measurement.TemperatureCallback
import com.dhealth.bluetooth.util.measurement.TemperatureUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_temperature.*
import org.koin.android.ext.android.inject


class TemperatureActivity : BaseActivity() {

    private var bleDevice: BleDevice? = null
    private val compositeDisposable: CompositeDisposable by inject()
    private lateinit var connection: Observable<RxBleConnection>
    private lateinit var connectionDisposable: Disposable
    private val lineDataset =  LineDataSet(ArrayList<Entry>(), "Data Temperature")
    private var isCelcius = true
    private var isPlay = false

    override fun onInitializedView(savedInstanceState: Bundle?) {
        bleDevice = dataReceived?.getParcelable(Extras.BLE_DEVICE)

        connectionDisposable = RxBus.subscribe(Consumer<Observable<RxBleConnection>> { connection ->
            this.connection = connection
        })

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setSupportActionBar(toolbar)
        toolbar.title = "${bleDevice?.device?.name} (${bleDevice?.device?.address})"

        chartDesign()

        rb_celcius.setOnCheckedChangeListener { buttonView, isChecked ->
            isCelcius = isChecked
            chart_temp.axisLeft.valueFormatter =
                TemperatureValueFormatter(
                    this,
                    isCelcius
                )
            chart_temp.clear()
            chart_temp.clearAnimation()
            lineDataset.clear()
            chart_temp.notifyDataSetChanged()
            chart_temp.invalidate()
        }

        rb_fahrenheit.setOnCheckedChangeListener { buttonView, isChecked ->
            isCelcius = !isChecked
            chart_temp.axisLeft.valueFormatter =
                TemperatureValueFormatter(
                    this,
                    isCelcius
                )
            chart_temp.clear()
            chart_temp.clearAnimation()
            lineDataset.clear()
            chart_temp.notifyDataSetChanged()
            chart_temp.invalidate()
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_temperature
    }

    override fun onDestroy() {
        connectionDisposable.dispose()
        super.onDestroy()
    }

    private fun chartDesign(){
        lineDataset.color = ContextCompat.getColor(this, R.color.black_effective)
        lineDataset.lineWidth = 2.0F
        lineDataset.setCircleColor(ContextCompat.getColor(this, R.color.colorPrimary))
        lineDataset.circleHoleColor = ContextCompat.getColor(this, R.color.colorAccent)
        lineDataset.setDrawCircles(true)
        lineDataset.setDrawValues(true)
        lineDataset.valueTextSize = 16F
        chart_temp.setNoDataTextColor(ContextCompat.getColor(this, R.color.black_effective))
        chart_temp.legend.isEnabled = false
        chart_temp.description.isEnabled = false
        chart_temp.xAxis.isEnabled = false
        chart_temp.axisRight.isEnabled = false
        chart_temp.axisLeft.isEnabled = true
        chart_temp.axisLeft.setDrawTopYLabelEntry(true)
        chart_temp.axisLeft.valueFormatter =
            TemperatureValueFormatter(this, isCelcius)
        chart_temp.setTouchEnabled(false)
        chart_temp.isAutoScaleMinMaxEnabled = true
        chart_temp.xAxis.axisMinimum = 0F
        chart_temp.xAxis.axisMaximum = 20F
        chart_temp.setVisibleXRangeMaximum(20F)
    }

    private fun renderDataSet(value: Float){
        if(chart_temp.data == null){
            chart_temp.data = LineData()
        }

        if(chart_temp.data.dataSets.isEmpty()){
            chart_temp.data.addDataSet(lineDataset)
        }

        val xValue = if(lineDataset.entryCount != 0){
            val data = lineDataset.getEntryForIndex(lineDataset.entryCount - 1)
            data.x + 1F
        } else {
            0F
        }

        addEntryToDataSet(Entry(xValue, value))

        if(xValue > 20){
            chart_temp.xAxis.resetAxisMinimum()
            chart_temp.xAxis.resetAxisMaximum()
        }

        chart_temp.data.notifyDataChanged()
        chart_temp.notifyDataSetChanged()
        chart_temp.moveViewToX(xValue)
    }

    private fun addEntryToDataSet(data: Entry){
        if(lineDataset.entryCount == 20){
            lineDataset.removeFirst()
        }

        lineDataset.addEntry(data)
    }

    private fun doMeasurement(){
        TemperatureUtil.commandInterval(compositeDisposable, connection, 500)
        TemperatureUtil.commandReadTemp(compositeDisposable, connection)
        TemperatureUtil.commandGetTemperature(compositeDisposable, connection, object : TemperatureCallback {
            override fun originalData(values: ByteArray) {
                Log.i("Data Notification", values.contentToString())
            }

            override fun temperatureInCelcius(value: Float) {
                Log.i("Suhu", MeasurementUtil.decimalFormat(value))
                if(isCelcius) {
                    runOnUiThread {
                        renderDataSet(value)
                        val suhu = MeasurementUtil.decimalFormat(value)
                        tv_celcius.text = "$suhu ${getString(R.string.derajat_celcius)}"
                    }
                }
            }

            override fun temperatureInFahrenheit(value: Float) {
                Log.i("Suhu Fahrenheit", MeasurementUtil.decimalFormat(value))
                if(!isCelcius){
                    runOnUiThread {
                        renderDataSet(value)
                    }
                }

                val suhu = MeasurementUtil.decimalFormat(value)
                tv_fahrenheit.text = "$suhu ${getString(R.string.derajat_fahrenheit)}"
            }

            override fun onError(error: Throwable) {
                Log.e("Error Notification", error.localizedMessage)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_menu, menu)
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
                doMeasurement()
            }
            R.id.nav_stop -> {
                isPlay = false
                MeasurementUtil.commandStop(compositeDisposable, connection)
            }
            R.id.nav_share -> {
                toast("Share")
            }
        }

        return super.onOptionsItemSelected(item)
    }
}