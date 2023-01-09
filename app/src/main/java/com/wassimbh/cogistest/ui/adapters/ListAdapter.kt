package com.wassimbh.cogistest.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.wassimbh.cogistest.R
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.databinding.ItItemBinding
import com.wassimbh.cogistest.utilities.OnRecyclerItemClick

class ListAdapter (private var list: List<PoiEntity>):  RecyclerView.Adapter<ListAdapter.ViewHolder>()  {

    private lateinit var clickListener: OnRecyclerItemClick<PoiEntity>
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        val itemBinding: ItItemBinding = ItItemBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity = list[position]
        holder.bind(entity)

        holder.viewBinding.apply {
            if(position == list.lastIndex)
                line.isVisible = false

            if(entity.isSelected){
                liParent.setBackgroundColor(getColor(R.color.cogis_blue))
                tvLabel.setTextColor(getColor(R.color.white))
                tvFloor.setTextColor(getColor(R.color.gray_light))
            }
            else{
                liParent.setBackgroundColor(getColor(R.color.transparent))
                tvLabel.setTextColor(getColor(R.color.black))
                tvFloor.setTextColor(getColor(R.color.gray))
            }
            root.setOnClickListener{
                clickListener.onRecycleItemClicked(entity, 0)
                entity.isSelected = true
                list.forEachIndexed{i, e->
                    if(i != position)
                        e.isSelected = false
                }
                notifyItemRangeChanged(0, list.size)
            }
        }
    }
    private fun getColor(colorId: Int): Int{
        return ContextCompat.getColor(context, colorId)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * An inner class that represents the item holder UI
     */
    inner class ViewHolder(val viewBinding: ItItemBinding) : RecyclerView.ViewHolder(viewBinding.root){
        /**
         * A method used to bind the holder with its entity
         * @param poiEntity
         */
        fun bind(poiEntity: PoiEntity){
            poiEntity.let {
                viewBinding.poi = it
            }
        }
    }

    fun updateList(l: MutableList<PoiEntity>){
        list = l
        notifyItemRangeChanged(0, l.size)
    }

    fun setupClickListener(listener: OnRecyclerItemClick<PoiEntity>){
        clickListener = listener
    }

}