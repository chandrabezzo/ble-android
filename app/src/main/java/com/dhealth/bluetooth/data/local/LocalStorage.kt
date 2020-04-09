package com.dhealth.bluetooth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dhealth.bluetooth.data.local.dao.EcgDao
import com.dhealth.bluetooth.data.local.dao.HrmDao
import com.dhealth.bluetooth.data.local.dao.TemperatureDao
import com.dhealth.bluetooth.data.model.Ecg
import com.dhealth.bluetooth.data.model.Hrm
import com.dhealth.bluetooth.data.model.Temperature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Temperature::class, Hrm::class, Ecg::class], version = 1)
abstract class LocalStorage: RoomDatabase() {

    abstract fun temperatureDao(): TemperatureDao
    abstract fun hrmDao(): HrmDao
    abstract fun ecgDao(): EcgDao

    companion object {
        @Volatile
        private var INSTANCE: LocalStorage? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope)
                : LocalStorage {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    LocalStorage::class.java,
                    "ble_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(LocalStorageCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(
            context: Context
        ): LocalStorage {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalStorage::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class LocalStorageCallback(
        private val scope: CoroutineScope
    ): RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let {
                scope.launch {
                    // initial local storage
                }
            }
        }
    }
}