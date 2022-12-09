package com.wassimbh.baseproject.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wassimbh.baseproject.api.ApiService
import com.wassimbh.baseproject.utilities.Coroutines
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val apiService: ApiService): ViewModel() {

    val test: MutableLiveData<Any> = MutableLiveData()

    fun getTestData(){
        Coroutines.io {
            val data = apiService.getTestApi()
            test.postValue(data)
        }
    }
}