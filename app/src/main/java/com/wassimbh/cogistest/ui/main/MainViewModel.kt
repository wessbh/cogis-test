package com.wassimbh.cogistest.ui.main

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.geojson.Polygon
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.turf.TurfMeasurement
import com.wassimbh.cogistest.R
import com.wassimbh.cogistest.dao.PoiDao
import com.wassimbh.cogistest.data.models.*
import com.wassimbh.cogistest.utilities.*
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class MainViewModel @Inject constructor(
    private val poiDao: PoiDao,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    resourcesProvider: ResourcesProvider
) : ViewModel() {

    private var floors = mutableListOf<Int>()
    val mLocations = ArrayList<MyLocation>()
    private val officeFileName = "rdc.geojson"
    var edges = mutableListOf<Edge>()
    val edgesLiveData = MutableLiveData<MutableList<GraphEdge>>()
    val itineraryList = MutableLiveData<List<ItineraryPoint>>()

    init {
        initData(resourcesProvider.context)
    }

    private fun initData(context: Context) {
        Coroutines.io {
            if (checkFirstUse()) {
                addDefaultPOIs()
            }
            val filesList = context.assets.list("cogis_geo")
            filesList?.let { list ->
                list.filter { it.split(".").last() == "geojson" }.forEach {
                    val strValue = context.getAssetJsonValue(it)
                    handleGeoJSONFiles(context, it, strValue)
                }
                floors.sortBy { it }
                floors = floors.distinct().toMutableList()

                list.filter { it.split(".").last() == "json" }.forEach {
                    handleJSONFiles(context.getAssetJsonValue(it))
                }
            }
            mLocations.sortBy { it.floorValue }
            initPositionListFromGeoJsonFile(mLocations.last().jsonStr)
        }
    }

    private fun handleGeoJSONFiles(context: Context, fileName: String, strValue: String) {
        val color = ContextCompat.getColor(
            context,
            if (fileName == officeFileName) R.color.office_color else R.color.floor_color
        )
        val opacity = if (fileName == officeFileName) 0.5 else 0.3
        val annotationOptionsList = ArrayList<PolygonAnnotationOptions>()
        val polygonList = getPolygonsFromGeoJson(strValue)
        polygonList.forEach { polygon ->
            val annotationOptions = PolygonAnnotationOptions()
                .withPoints(polygon.coordinates())
                .withFillColor(color)
                .withFillOpacity(opacity)
            annotationOptionsList.add(annotationOptions)
        }
        val mLocation = MyLocation(
            fileName,
            strValue,
            annotationOptionsList
        )
        if (fileName == officeFileName) {
            mLocation.floorType = FloorsEnum.RDC
            mLocation.floorValue = 1
            mLocation.withPolyLine = true
            mLocation.isDisplayed = true
        } else {
            mLocation.floorValue = 0
            mLocation.floorType = FloorsEnum.SOUS_SOL
        }
        floors.add(mLocation.floorValue)
        mLocation.poiList = retrievePOIByFloor(mLocation.floorType.name).toMutableList()
        mLocations.add(mLocation)
    }

    private fun handleJSONFiles(strValue: String) {
        initItineraryPoints(strValue)
    }

    private fun initItineraryPoints(jsonStr: String) {
        val itineraryPoints = ArrayList<ItineraryPoint>()
        val gson = Gson()
        val jsonObject = JSONObject(jsonStr)
        val nodesArray = jsonObject.getJSONArray("nodes")
        for (i in 0 until nodesArray.length()) {
            val point = gson.fromJson(nodesArray.get(i).toString(), ItineraryPoint::class.java)
            itineraryPoints.add(point)
        }
        val edgesArray = jsonObject.getJSONArray("edges")
        val edgesList = ArrayList<GraphEdge>()
        for (i in 0 until edgesArray.length()) {
            val begin = edgesArray.getJSONObject(i).getInt("begin")
            val end = edgesArray.getJSONObject(i).getInt("end")
            val beginPoint =
                fromLngLat(itineraryPoints[begin].longitude, itineraryPoints[begin].latitude)
            val endPoint = fromLngLat(itineraryPoints[end].longitude, itineraryPoints[end].latitude)
            val distance = TurfMeasurement.distance(beginPoint, endPoint) * 1000
            val floorChanged = itineraryPoints[begin].floor != itineraryPoints[end].floor
            val floorValue =
                if (itineraryPoints[begin].floor == itineraryPoints[end].floor) itineraryPoints[begin].floor else null
            val edgeEntity = GraphEdge(
                begin,
                end,
                distance,
                Pair(beginPoint, endPoint),
                floorChanged,
                floorValue = floorValue,
                from = if (floorChanged) itineraryPoints[begin].floor else null,
                to = if (floorChanged) itineraryPoints[end].floor else null,
            )
            edgesList.add(edgeEntity)
        }
        edgesList.sortBy { it.start }
        edgesList.forEach {
            edges.add(Edge(PathNode(it.start), PathNode(it.destination), it.distance))
        }
        edges.sortedBy { (it.node1 as PathNode).number }
        edgesLiveData.postValue(edgesList)
        itineraryList.postValue(itineraryPoints)
    }

    private fun getPolygonsFromGeoJson(jsonStr: String): ArrayList<Polygon> {

        // Get GeoJSON features from GeoJSON file in the assets folder
        val featureCollection =
            FeatureCollection.fromJson(jsonStr)


        val polygonList = ArrayList<Polygon>()

        // Get the position of each GeoJSON feature and build the list of Position
        // objects for eventual use in the Matrix API call
        if (featureCollection.features() != null) {
            for (singleLocation in featureCollection.features()!!) {
                if (singleLocation.geometry() is Polygon) {
                    polygonList.add(singleLocation.geometry() as Polygon)
                }
            }
        }
        return polygonList
    }


    private fun retrievePOIByFloor(floorName: String): List<PoiEntity> {
        return poiDao.selectByFloor(floorName)
    }

    private fun addDefaultPOIs() {
        Coroutines.io {
            val poiList = mutableListOf(
                PoiEntity(0, 2.5006494348169497, 48.75190181298453, "Office1", FloorsEnum.RDC.name),
                PoiEntity(0, 2.5006735648296683, 48.75183393787151, "Office2", FloorsEnum.RDC.name),
                PoiEntity(0, 2.500782574446646, 48.75192612643988, "Kitchen", FloorsEnum.RDC.name),
                PoiEntity(0, 2.5010102616347467, 48.7519227632784, "Lounge", FloorsEnum.RDC.name),
                PoiEntity(
                    0,
                    2.5007650444905494,
                    48.75208178006045,
                    "Parking",
                    FloorsEnum.SOUS_SOL.name
                )
            )

            poiDao.insertPOIList(poiList)
        }
        sharedPreferencesProvider.insertBoolean("firsTime", false)
    }

    private fun initPositionListFromGeoJsonFile(jsonStr: String?): List<Point> {

        // Initialize List<Position> for eventual use in the Matrix API call
        val pointList = ArrayList<Point>()
        // Get GeoJSON features from GeoJSON file in the assets folder
        jsonStr?.let {
            val featureCollection =
                FeatureCollection.fromJson(it)

            if (featureCollection.features() != null) {
                for (singleLocation in featureCollection.features()!!) {
                    if (singleLocation.geometry() is Point) {
                        pointList.add(singleLocation.geometry() as Point)
                    }
                }
            }
        }
        return pointList
    }

    private fun checkFirstUse(): Boolean {
        return sharedPreferencesProvider.getBool("firsTime", true)
    }

    fun getClosestChangingFloorEdges(
        startPoint: PoiEntity,
        endPoint: PoiEntity,
        edges: List<GraphEdge>
    ): GraphEdge? {
        var edge: GraphEdge? = null
        var minStartDist = Double.MAX_VALUE
        var minEndDist = Double.MAX_VALUE

        edges.filter { it.floorChange }
            .forEach {
                val startDistance = getEdgeMinDistance(startPoint.toPoint(), it)
                val endDistance = getEdgeMinDistance(endPoint.toPoint(), it)
                val toTargetFloor =
                    (it.from == endPoint.getFloorValue() || it.to == endPoint.getFloorValue())

                if (startDistance < minStartDist && endDistance < minEndDist && toTargetFloor) {
                    minStartDist = startDistance
                    minEndDist = endDistance
                    edge = it
                }
            }

        return edge
    }

    private fun getEdgeMinDistance(point: Point, edge: GraphEdge): Double {
        val dist1 = TurfMeasurement.distance(point, edge.point.first)
        val dist2 = TurfMeasurement.distance(point, edge.point.second)
        return min(dist1, dist2)
    }

    fun getEdgeFromList(
        point: Point,
        edges: List<GraphEdge>,
        calculateBeginning: Boolean
    ): GraphEdge? {
        var edge: GraphEdge? = null
        var minDistance = 10.0
        edges.forEach {
            val destination = if (calculateBeginning) it.point.first else it.point.second
            val distance = TurfMeasurement.distance(point, destination)
            if (distance < minDistance) {
                minDistance = distance
                edge = it
            }
        }

        return edge
    }

}