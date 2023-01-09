package com.wassimbh.cogistest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.databinding.PoiItemBinding
import com.wassimbh.cogistest.utilities.OnRecyclerItemClick

class PoiAdapter:  RecyclerView.Adapter<PoiAdapter.ViewHolder>() {

    private lateinit var clickListener: OnRecyclerItemClick<PoiEntity>
    private lateinit var list: MutableList<PoiEntity>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding: PoiItemBinding = PoiItemBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity = list[position]
        holder.bind(entity)

        holder.viewBinding.apply {
            ivDelete.setOnClickListener{
                clickListener.onRecycleItemClicked(entity, -1)
                list.removeAt(position)
                notifyItemRemoved(position)
            }
            ivEdit.setOnClickListener{
                llIcons.isVisible = false
                tvConfirm.isVisible = true
                etLabel.isEnabled = true
                etLabel.requestFocus()
                etLabel.setSelection(etLabel.length())
            }
            tvConfirm.setOnClickListener{
                llIcons.isVisible = true
                tvConfirm.isVisible = false
                entity.label = etLabel.text.toString()
                etLabel.isEnabled = false
                clickListener.onRecycleItemClicked(entity, 0)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * An inner class that represents the item holder UI
     */
    inner class ViewHolder(val viewBinding: PoiItemBinding) : RecyclerView.ViewHolder(viewBinding.root){
        /**
         * A method used to bind the holder with its entity
         * @param poiEntity
         */
        fun bind(poiEntity: PoiEntity){
            viewBinding.poi = poiEntity
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