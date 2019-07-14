package com.timgortworst.roomy.ui.customview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timgortworst.roomy.R
import com.timgortworst.roomy.model.BottomMenuItem
import kotlinx.android.synthetic.main.item_bottom_sheet_menu.view.*

class BottomSheetMenuAdapter(private val items: List<BottomMenuItem>) :
    RecyclerView.Adapter<BottomSheetMenuAdapter.BottomSheetMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetMenuViewHolder {
        return BottomSheetMenuViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bottom_sheet_menu,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BottomSheetMenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class BottomSheetMenuViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: BottomMenuItem) {
            with(view) {
                bottom_menu_title.text = item.name
                bottom_menu_icon.setImageResource(item.resId)

                setOnClickListener { item.action() }
            }
        }
    }

}