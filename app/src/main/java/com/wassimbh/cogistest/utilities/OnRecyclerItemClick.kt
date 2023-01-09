package com.wassimbh.cogistest.utilities

import android.view.View

interface OnRecyclerItemClick<T> {
    fun onRecycleItemClicked(entity: T, action: Int) {
    }
}