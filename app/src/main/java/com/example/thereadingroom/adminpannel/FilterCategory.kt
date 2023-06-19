package com.example.thereadingroom.adminpannel

import android.widget.Filter

class FilterCategory: Filter {

    private var filterList: ArrayList<ModelCategory>
    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory): super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel: ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(constraint)) {
                    filterModel.add(filterList[i])
                }
            }
            results.count = filterModel.size
            results.values = filterModel
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterCategory.categoryArrayList  = results.values as ArrayList<ModelCategory>

        adapterCategory.notifyDataSetChanged()
    }



}