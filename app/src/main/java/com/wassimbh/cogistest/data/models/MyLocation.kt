package com.wassimbh.cogistest.data.models

import androidx.room.Ignore
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.wassimbh.cogistest.utilities.FloorsEnum

data class MyLocation(
    val fileName: String,
    val jsonStr: String,
    val annotationOptions: ArrayList<PolygonAnnotationOptions>,
    var floorType: FloorsEnum = FloorsEnum.UNKNOWN,
    var floorValue: Int = 0,
    var withPolyLine: Boolean = false,
    var isDisplayed: Boolean = false,
    var polygonAnnotations: List<PolygonAnnotation>? = null,
    var polylineAnnotation: List<PolylineAnnotation>? = null,
    var pointAnnotation: MutableList<PointAnnotation> = mutableListOf(),
    var centerPoint: Point? = null,
    @Ignore
    var poiList: MutableList<PoiEntity> = mutableListOf<PoiEntity>()
)