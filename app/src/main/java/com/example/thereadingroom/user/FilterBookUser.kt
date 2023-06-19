package com.example.thereadingroom.user

import android.widget.Filter
import com.example.thereadingroom.adminpannel.books.ModelPDF

class FilterBookUser: Filter {

    var filterList: ArrayList<ModelPDF>
    var adapterPDF: PDFUserAdapter

    constructor(filterList: ArrayList<ModelPDF>, adapterPDF: PDFUserAdapter): super() {
        this.filterList = filterList
        this.adapterPDF = adapterPDF
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel = ArrayList<ModelPDF>()
            for (i in filterList.indices) {
                if (filterList[i].title.uppercase().contains(constraint)) {
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
        adapterPDF.pdfArrayList  = results.values as ArrayList<ModelPDF>

        adapterPDF.notifyDataSetChanged()
    }


}