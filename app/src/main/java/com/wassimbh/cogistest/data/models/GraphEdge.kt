package com.wassimbh.cogistest.data.models

import com.mapbox.geojson.Point
import com.wassimbh.cogistest.utilities.Node

data class GraphEdge(
    val start: Int,
    val destination: Int,
    val distance: Double,
    var point: Pair<Point, Point>,
    val floorChange: Boolean = false,
    val floorValue: Int? = null,
    val from: Int? = null,
    val to: Int? = null
): Node{
    fun reversePoints(){
        this.point = Pair(this.point.second, this.point.first)
    }
}
