package com.wassimbh.cogistest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<D : ViewDataBinding> : Fragment() {

    open lateinit var mDataBinding: D

    @get:LayoutRes
    protected abstract val layoutResourceId: Int



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mDataBinding = DataBindingUtil.inflate(inflater, layoutResourceId, container, false)
        return mDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding.unbind()
    }

    open fun viewModelObserver() {
        // Will be implemented into the Fragments implemented this class
    }

    open fun setUpView() {
        // Will be implemented into the Fragments implemented this class
    }

    companion object{
        val TAG = this::class.java.simpleName
    }
}