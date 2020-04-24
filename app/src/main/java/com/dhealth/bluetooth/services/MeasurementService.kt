package com.dhealth.bluetooth.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.bezzo.core.BuildConfig
import com.bezzo.core.extension.toast
import com.bezzo.core.util.TelegramLogger
import com.dhealth.bluetooth.R
import com.dhealth.bluetooth.data.constant.AppConstants
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.model.Temperature
import com.dhealth.bluetooth.ui.MeasurementSetting
import com.dhealth.bluetooth.util.measurement.*
import com.dhealth.bluetooth.viewmodel.EcgViewModel
import com.dhealth.bluetooth.viewmodel.HrmViewModel
import com.dhealth.bluetooth.viewmodel.MeasurementViewModel
import com.dhealth.bluetooth.viewmodel.TemperatureViewModel
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import org.koin.core.inject


class MeasurementService: Service(), KoinComponent {

    private val viewmodel: MeasurementViewModel by inject()
    private val tempVM: TemperatureViewModel by inject()
    private val ecgVM: EcgViewModel by inject()
    private val hrmVM: HrmViewModel by inject()
    private val bleClient: RxBleClient by inject()

    private var connection: Observable<RxBleConnection>? = null
    private var compositeDisposable: CompositeDisposable? = null
    private var startMeasurement = false

    companion object {
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }

    private val ACTION_PAUSE = "ACTION_PAUSE"
    private val ACTION_PLAY = "ACTION_PLAY"
    private var notificationId = 1
    private var isDefaultEcg = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            isDefaultEcg = intent.getBooleanExtra(AppConstants.DEFAULT_ECG, false)
            if (action != null) when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    notificationId = System.currentTimeMillis().toInt()

                    foregroundService("Stand By", false)

                    connection = bleClient.getBleDevice(viewmodel.selectedDevice())
                        .establishConnection(false).compose(ReplayingShare.instance())
                }
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopForegroundService()
                }
                ACTION_PLAY -> {
                    foregroundService("Checking...", false)

                    if(startMeasurement) stopMeasurement()

                    compositeDisposable = CompositeDisposable()
                    connection?.let { connection ->
                        compositeDisposable?.add(MeasurementUtil.commandGetDeviceInfo(connection))
                        compositeDisposable?.add(MeasurementUtil.commandCreateSetTime(connection))
                        compositeDisposable?.add(MeasurementUtil.commandSetStreamTypeToBin(connection))
                    }

                    when(viewmodel.measurementType()){
                        1 -> startTemp()
                        2 -> startHrm()
                        3 -> startEcg()
                    }
                }
                ACTION_PAUSE -> {
                    foregroundService("Pause", false)
                    stopMeasurement()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun stopMeasurement(){
        connection?.let { MeasurementUtil.commandStop(it) }
        startMeasurement = false
        compositeDisposable?.dispose()
        toast("Stop Measurement")
        viewmodel.isChecking(false)
    }

    private fun startTemp(){
        connection?.let { connection ->
            compositeDisposable?.add(TemperatureUtil.commandInterval(connection, 1000))
            compositeDisposable?.add(TemperatureUtil.commandReadTemp(connection))
            compositeDisposable?.add(TemperatureUtil.commandGetTemperature(connection,
                object : TemperatureCallback{
                    override fun originalData(values: ByteArray) {
                        Log.i("Original Data", values.contentToString())
                    }

                    override fun temperature(id: Long, celcius: Float, fahrenheit: Float) {
                        Log.i("Temperature", "$celcius °C | $fahrenheit °F")
                        if(!startMeasurement) toast("Start Measurement"); viewmodel.isChecking(true)
                        startMeasurement = true

                        tempVM.add(Temperature(celcius, fahrenheit, MeasurementUtil.getEpoch()))
                    }

                    override fun onError(error: Throwable) {
                        TelegramLogger(this@MeasurementService, BuildConfig.TELEGRAM_BOT, error.message,
                            getString(R.string.developer_id), BuildConfig.TELEGRAM_ID).send()
                    }
                }
            ))
        }
    }

    private fun startHrm(){
        connection?.let { connection ->
            compositeDisposable?.add(HrmUtil.commandMinConfidenceLevel(connection, 0))
            compositeDisposable?.add(HrmUtil.commandHrExpireDuration(connection, 30))
            compositeDisposable?.add(HrmUtil.commandReadHrm(connection))
            compositeDisposable?.add(HrmUtil.commandGetHrm(connection,
                object: HrmCallback{
                    override fun originalData(values: ByteArray) {
                        Log.i("Original Data", values.contentToString())
                    }

                    override fun heartRateMonitor(
                        id: Long,
                        channel1: Int,
                        channel2: Int,
                        heartRate: Int,
                        confidence: Int,
                        activity: String,
                        accelerationX: Float,
                        accelerationY: Float,
                        accelerationZ: Float,
                        spo2: Float
                    ) {
                        if(confidence >= 90){
                            Log.i("HRM", heartRate.toString())
                            if(!startMeasurement) toast("Start Measurement"); viewmodel.isChecking(true)
                            startMeasurement = true

                            hrmVM.add(Hrm(channel1, channel2, accelerationX, accelerationY, accelerationZ,
                                heartRate, confidence, spo2, activity, MeasurementUtil.getEpoch()))
                        }
                        else {
                            Log.i("Confidence", "Under 90")
                        }
                    }
                }
            ))
        }
    }

    private fun startEcg(){
        val movingAverage = MovingAverage(5)
        connection?.let { connection ->
            if(isDefaultEcg){
                for(entry in Ecg.defaults.entries){
                    val key = entry.key
                    val value = entry.value

                    compositeDisposable?.add(EcgUtil.commandSendDefaultRegisterValues(connection, key, value))
                }
            }
            else {
                compositeDisposable?.add(EcgUtil.commandCreateGetRegister(connection))
            }

            compositeDisposable?.add(EcgUtil.commandReadEcg(connection))
            compositeDisposable?.add(EcgUtil.commandGetEcg(connection, isDefaultEcg, movingAverage,
                object: EcgCallback {
                    override fun originalData(values: ByteArray) {
                        Log.i("Original Data", values.contentToString())
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
                        if(!startMeasurement) toast("Start Measurement"); viewmodel.isChecking(true)
                        startMeasurement = true

                        ecgVM.add(Ecg(
                            ecg, eTag, pTag, rTor, currentRToRBpm, ecgMv, filteredEcg, averageRToRBpm,
                            counterToReport, MeasurementUtil.getEpoch()
                        ))
                    }

                }
            ))
        }
    }

    private fun foregroundService(action: String, isCancel: Boolean) {
        val notification = createNotification("measurement", "Measurement Service", action)
        val notificationManager = NotificationManagerCompat.from(this)
        if(isCancel){
            notificationManager.cancel(notificationId)
            stopMeasurement()
        }
        else {
            notificationManager.notify(notificationId, notification)
            stopForeground(true)
            startForeground(notificationId, notification)
        }
    }

    private fun playAction(): NotificationCompat.Action {
        val playIntent = Intent(this, MeasurementService::class.java)
        playIntent.action = ACTION_PLAY
        val pendingIntent = PendingIntent.getService(this, 0, playIntent, 0)

        return NotificationCompat.Action(
            android.R.drawable.ic_media_play,
            "Play",
            pendingIntent
        )
    }

    private fun pauseAction(): NotificationCompat.Action {
        val pauseIntent = Intent(this, MeasurementService::class.java)
        pauseIntent.action = ACTION_PAUSE
        val pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0)
        return NotificationCompat.Action(
            android.R.drawable.ic_media_pause,
            "Pause",
            pendingPrevIntent
        )
    }

    private fun stopAction(): NotificationCompat.Action {
        val stopIntent = Intent(this, MeasurementService::class.java)
        stopIntent.action = ACTION_STOP_FOREGROUND_SERVICE
        val pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0)
        return NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            pendingStopIntent
        )
    }

    private fun createNotification(
        channelId: String,
        channelName: String,
        action: String
    ): Notification {
        val resultIntent = Intent(this, MeasurementSetting::class.java)
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)

        if(Build.VERSION.SDK_INT >= 26){
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            val manager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)

        if(Build.VERSION.SDK_INT >= 24){
            notificationBuilder.priority = NotificationManager.IMPORTANCE_HIGH
        }
        else {
            notificationBuilder.priority = Notification.PRIORITY_HIGH
        }

        if(Build.VERSION.SDK_INT >= 21){
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
        }

        var measurement = "Measurement"
        when(viewmodel.measurementType()){
            1 -> measurement = "Temperature Measurement"
            2 -> measurement = "Heart Rate Measurement"
            3 -> measurement = "Electrocardiogram Measurement"
        }

        return notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(measurement)
            .setContentText(action)
            .addAction(playAction())
            .addAction(pauseAction())
            .build()
    }

    private fun stopForegroundService() {
        Log.d("FOREGROUND_SERVICE", "Stop foreground service.")
        stopMeasurement()
        connection = null
        stopForeground(true)
        stopSelf()
    }
}