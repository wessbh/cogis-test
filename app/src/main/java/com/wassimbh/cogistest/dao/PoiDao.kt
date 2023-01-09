package com.wassimbh.cogistest.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wassimbh.cogistest.data.models.PoiEntity

/**
 * Data access object that will contain the list of methods that will
 * perform SQL Requests from the Room database
 */
@Dao
interface PoiDao {

    /**
     * Insert a PoiEntity in the local database if an item already
     * exists in the database, it will be replaced
     * @param entity marker that will be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPOI(entity: PoiEntity): Long
    /**
     * Insert a  list of PoiEntity in the local database if an item already
     * exists in the database, it will be replaced
     * @param list poi list
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPOIList(list: List<PoiEntity>)

    /**
     * Insert a list of PoiEntity in the local database if an item already
     * exists in the database, it will be replaced
     * @param entity Marker that will be updated
     */
    @Update
    fun updatePOI(entity: PoiEntity)

    /**
     * Retrieve a list of PoiEntity from the local database
     * @return list of PoiEntity
     */
    @Query("SELECT * FROM PoiEntity")
    fun selectAll(): List<PoiEntity>

    /**
     * Retrieve a list of PoiEntity from the local database by floor
     * @return list of PoiEntity
     */
    @Query("SELECT * FROM PoiEntity WHERE floor= :floorName")
    fun selectByFloor(floorName: String): List<PoiEntity>

    /**
     * Retrieve a PoiEntity from the local database
     * @return PoiEntity
     */
    @Query("SELECT * FROM PoiEntity WHERE id = :id")
    fun selectById(id: Long): PoiEntity
    /**
     * Retrieve a list of PoiEntity from the local database
     * @return list of PoiEntity
     */
    @Delete
    fun delete(entity: PoiEntity)

}