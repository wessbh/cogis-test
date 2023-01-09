package com.wassimbh.cogistest.ui.data

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
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
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(private val poiDao: PoiDao, private val sharedPreferencesProvider: SharedPreferencesProvider)  : ViewModel() {

    val poiList = MutableLiveData<List<PoiEntity>>()


    fun getPoiList(){
        Coroutines.io {
            val list = poiDao.selectAll()
            poiList.postValue(list)
        }
    }

    fun deletePoi(poiEntity: PoiEntity){
        Coroutines.io {
            poiDao.delete(poiEntity)
        }
    }

    fun updatePoi(poiEntity: PoiEntity){
        Coroutines.io {
            poiDao.updatePOI(poiEntity)
        }
    }

}