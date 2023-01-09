package com.wassimbh.cogistest.ui.map

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.mapbox.geojson.*
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.wassimbh.cogistest.R
import com.wassimbh.cogistest.dao.PoiDao
import com.wassimbh.cogistest.data.models.GraphEdge
import com.wassimbh.cogistest.data.models.ItineraryPoint
import com.wassimbh.cogistest.data.models.MyLocation
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.utilities.Coroutines
import com.wassimbh.cogistest.utilities.FloorsEnum
import com.wassimbh.cogistest.utilities.SharedPreferencesProvider
import com.wassimbh.cogistest.utilities.getAssetJsonValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val poiDao: PoiDao, private val sharedPreferencesProvider: SharedPreferencesProvider) : ViewModel() {

    val edgesList = ArrayList<GraphEdge>()
    val floorsHashMap = HashMap<String, String>()
    val mLocations = ArrayList<MyLocation>()
    val poiList = MutableLiveData<List<PoiEntity>>()
    val itineraryList = MutableLiveData<List<ItineraryPoint>>()
    private val officeFileName = "rdc.geojson"

    fun initData(context: Context) {
        Coroutines.io{
            if(checkFirstUse()){
                addDefaultPOIs()
            }
            poiList.postValue(poiDao.selectAll().sortedBy { it.floor })
            context.assets.list("cogis_geo")?.forEach { fileName ->
                val fileType = fileName.split(".").last()
                val strValue = context.getAssetJsonValue(fileName)
                when (fileType) {
                    "geojson" -> {
                        floorsHashMap[fileName] = strValue
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
                        } else {
                            mLocation.floorValue = 0
                            mLocation.floorType = FloorsEnum.SOUS_SOL
                        }
                        mLocation.poiList = retrievePOIByFloor(mLocation.floorType.name).toMutableList()
                        mLocations.add(mLocation)
                    }
                    "json" -> {
                        val f1 = "wayfinding_graph.json"
                        val f2 = "first_wayfinding_graph.json"
                        if(fileName == f2){
                            initItineraryPoints(strValue)
                        }
                    }
                }
            }
            mLocations.sortBy { it.floorValue }
            initPositionListFromGeoJsonFile(mLocations.last().jsonStr)
        }
    }

    private fun retrievePOIByFloor(floorName: String): List<PoiEntity>{
        return poiDao.selectByFloor(floorName)
    }
    fun getPoi(floorName: String): LiveData<List<PoiEntity>> = liveData(Dispatchers.IO){
        emit(poiDao.selectByFloor(floorName))
    }
    fun resetSelection(){
        var list = emptyList<PoiEntity>()
        poiList.value?.let {
            it.forEach{
                it.isSelected = false
            }
            list = it
        }
        poiList.value = list
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
        for (i in 0 until edgesArray.length()) {
            val edges = gson.fromJson(edgesArray.get(i).toString(), GraphEdge::class.java)
            edgesList.add(edges)
        }
        itineraryList.postValue(itineraryPoints)
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

    fun getPolygonsFromGeoJson(jsonStr: String): ArrayList<Polygon> {

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

    private fun getLayer(location: MyLocation): Layer {
        val sourceID = location.fileName
        val featureCollection = FeatureCollection.fromJson(floorsHashMap[sourceID]!!)
        val layer = FillLayer(sourceID, sourceID)

        if (!featureCollection.features().isNullOrEmpty()) {
            featureCollection.features()!!.forEach { feature ->
                if (feature.getStringProperty("ETAGE") != null) {
                    layer.fillColor("#028CA1")
                }
                if (feature.getStringProperty("bureau") != null) {
                    layer.fillColor("#445597")
                }
            }
        }
        return layer
    }


    private fun getLineStringsFromGeoJson(jsonStr: String): List<LineString> {

        val featureCollection =
            FeatureCollection.fromJson(jsonStr)

        val lineStringList = ArrayList<LineString>()

        if (featureCollection.features() != null) {
            for (singleLocation in featureCollection.features()!!) {
                if (singleLocation.geometry() is Polygon) {
                    (singleLocation.geometry() as Polygon).outer()?.let {
                        lineStringList.add(it)
                    }
                }
            }
        }
        return lineStringList
    }

    private fun getBoundingBox(coordinates: ArrayList<Point>): BoundingBox {
        var west = 2.5008
        var east = 2.5008
        var north = 48.75203
        var south = 48.75203

        coordinates.forEach { loc ->
            north = loc.latitude().coerceAtLeast(north)
            south = loc.latitude().coerceAtMost(south)
            west = loc.longitude().coerceAtMost(west)
            east = loc.longitude().coerceAtMost(east)
        }

        // Add some extra "padding"
        val padding = 0.01
        north += padding
        south -= padding
        west -= padding
        east += padding

        return BoundingBox.fromLngLats(west, south, east, north)
    }

    private fun findCenterFromBoundingBox(bound: BoundingBox): Point{
        val latitude = (bound.southwest().latitude()+  bound.northeast().latitude())/2
        val longitude = (bound.southwest().longitude()+  bound.northeast().longitude())/2

        return Point.fromLngLat(longitude, latitude)
    }

    fun insertMarker(poiEntity: PoiEntity){
        Coroutines.io {
            poiDao.insertPOI(poiEntity)
        }
    }
    fun selectMarkerById(id: Long): PoiEntity{
        return poiDao.selectById(id)
    }

    fun updateMarker(poiEntity: PoiEntity){
        Coroutines.io {
            poiDao.insertPOI(poiEntity)
        }
    }


    private fun addDefaultPOIs(){
        Coroutines.io {
            val poiList = mutableListOf(
                PoiEntity(0, 2.5006494348169497, 48.75190181298453, "Office1", FloorsEnum.RDC.name),
                PoiEntity(0, 2.5006735648296683, 48.75183393787151, "Office2", FloorsEnum.RDC.name),
                PoiEntity(0, 2.500782574446646, 48.75192612643988, "Kitchen", FloorsEnum.RDC.name),
                PoiEntity(0, 2.5010102616347467, 48.7519227632784, "Lounge", FloorsEnum.RDC.name),
                PoiEntity(0, 2.5007650444905494, 48.75208178006045, "Parking", FloorsEnum.SOUS_SOL.name)
            )

            poiDao.insertPOIList(poiList)
        }
        sharedPreferencesProvider.insertBoolean("firsTime", false)
    }

    private fun checkFirstUse(): Boolean{
        return sharedPreferencesProvider.getBool("firsTime", true)
    }

    private fun calculateItinerary(start: Point, destination: Point): List<Point>{
        val itinerary = ArrayList<Point>()


        return itinerary
    }
}