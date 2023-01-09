package com.wassimbh.cogistest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wassimbh.cogistest.dao.PoiDao
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.utilities.Constants


/**
 * Abstract class that will represent the Room database
 */
@Database(entities = [PoiEntity::class] , version = Constants.DB_VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * an abstract method to get the PoiEntity
     * @return instance of the PoiDao
     */
    abstract fun getPoiDao(): PoiDao
}