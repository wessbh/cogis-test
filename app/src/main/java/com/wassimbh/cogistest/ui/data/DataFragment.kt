package com.wassimbh.cogistest.ui.data

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wassimbh.cogistest.R
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.databinding.FragmentDataBinding
import com.wassimbh.cogistest.ui.base.BaseFragment
import com.wassimbh.cogistest.ui.adapters.PoiAdapter
import com.wassimbh.cogistest.utilities.OnRecyclerItemClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataFragment : BaseFragment<FragmentDataBinding>(), OnRecyclerItemClick<PoiEntity> {

    override val layoutResourceId = R.layout.fragment_data
    private val mViewModel: DataViewModel by viewModels()

    private lateinit var poiAdapter: PoiAdapter

    override fun setUpView() {
        super.setUpView()
        poiAdapter = PoiAdapter()
        poiAdapter.setupClickListener(this)
    }

    override fun viewModelObserver() {
        super.viewModelObserver()
        mViewModel.poiList.observe(viewLifecycleOwner){
            poiAdapter.updateList(it.toMutableList())
            mDataBinding.rvPoi.apply {
                adapter = poiAdapter
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            }
        }

        mViewModel.getPoiList()
    }

    override fun onRecycleItemClicked(entity: PoiEntity, action: Int) {
        super.onRecycleItemClicked(entity, action)
        when(action){
            -1 -> {
                mViewModel.deletePoi(entity)
            }
            0 -> {
                mViewModel.updatePoi(entity)
            }
        }
    }
}