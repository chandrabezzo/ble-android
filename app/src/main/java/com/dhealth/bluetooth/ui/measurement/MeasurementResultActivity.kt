package com.dhealth.bluetooth.ui.measurement

import android.os.Bundle
import com.bezzo.core.base.BaseActivity
import com.bezzo.core.extension.launchFragment
import com.dhealth.bluetooth.R
import kotlinx.android.synthetic.main.activity_measurement_result.*

class MeasurementResultActivity : BaseActivity() {
    override fun onInitializedView(savedInstanceState: Bundle?) {
        setSupportActionBar(toolbar)

        launchFragment(R.id.fl_measurement_result, TemperatureMeasurementFragment::class.java)

        bnv_measurement_result.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_hrm -> {
                    launchFragment(R.id.fl_measurement_result, HrmMeasurementFragment::class.java)
                    true
                }
                R.id.nav_ecg -> {
                    launchFragment(R.id.fl_measurement_result, EcgMeasurementFragment::class.java)
                    true
                }
                else -> {
                    launchFragment(R.id.fl_measurement_result, TemperatureMeasurementFragment::class.java)
                    true
                }
            }
        }
    }

    override fun setLayout(): Int {
        return R.layout.activity_measurement_result
    }
}
