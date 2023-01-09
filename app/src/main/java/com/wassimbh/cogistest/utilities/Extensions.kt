package com.wassimbh.cogistest.utilities

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.wassimbh.cogistest.data.models.GraphEdge
import com.wassimbh.cogistest.data.models.ItineraryPoint
import com.wassimbh.cogistest.data.models.PoiEntity


fun Context.getAssetJsonValue(sourceName: String): String{
    return this.assets.open("cogis_geo/$sourceName").bufferedReader().use { it.readText() }
}

fun List<ItineraryPoint>.toPoints(): List<Point> {
    return map { fromLngLat(it.longitude, it.latitude) }
}

fun GraphEdge.toPoi(fromStart: Boolean): PoiEntity{
    val edgePoint = if(fromStart) point.first else point.second
    return PoiEntity(0, edgePoint.longitude(), edgePoint.latitude(), "", "")
}

fun List<Edge>.reverseNodes(): List<Edge>{
    return map { Edge(it.node2, it.node1, it.distance) }
}