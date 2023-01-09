package com.wassimbh.cogistest.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.wassimbh.cogistest.dao.PoiDao
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.utilities.Coroutines
import com.wassimbh.cogistest.utilities.SharedPreferencesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val poiDao: PoiDao) : ViewModel() {

    val poiList = MutableLiveData<List<PoiEntity>>()
    init {
        Coroutines.io{ poiList.postValue(poiDao.selectAll().sortedBy { it.floor }) }
    }
    fun getPoi(floorName: String): LiveData<List<PoiEntity>> = liveData(Dispatchers.IO){
        emit(poiDao.selectByFloor(floorName))
    }

    fun resetSelection(){
        Coroutines.io{
            var list = emptyList<PoiEntity>()
            poiList.value?.let {l->
                l.forEach {
                    it.isSelected = false
                }
                list = l
            }
            poiList.postValue(list)
        }
    }

    fun insertMarker(poiEntity: PoiEntity):LiveData<PoiEntity> = liveData(Dispatchers.IO){
        val id = poiDao.insertPOI(poiEntity)
        emit(selectMarkerById(id))
    }
    fun selectMarkerById(id: Long): PoiEntity{
        return poiDao.selectById(id)
    }

    fun updateMarker(poiEntity: PoiEntity){
        Coroutines.io {
            poiDao.insertPOI(poiEntity)
        }
    }

}