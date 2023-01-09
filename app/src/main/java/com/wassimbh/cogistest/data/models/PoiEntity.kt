package com.wassimbh.cogistest.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.wassimbh.cogistest.utilities.FloorsEnum

@Entity
data class PoiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var longitude: Double,
    var latitude: Double,
    var label: String,
    val floor: String
){
    @Ignore
    var isSelected: Boolean = false

    fun toPoint(): Point {
        return fromLngLat(this.longitude, this.latitude)
    }
    fun getFloorValue():Int{
        return when(this.floor){
            FloorsEnum.SOUS_SOL.name -> {-1}
            FloorsEnum.RDC.name -> {0}
            else->{-2}
        }
    }
}