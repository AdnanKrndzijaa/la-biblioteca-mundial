package com.example.thereadingroom.adminpannel.books

import android.widget.Filter

class PDFFilterAdmin : Filter {

    private val filterList: ArrayList<ModelPDF>
    private val pdfAdapterAdmin: PDFAdapterAdmin

    constructor(filterList: ArrayList<ModelPDF>, pdfAdapterAdmin: PDFAdapterAdmin) : super() {
        this.filterList = filterList
        this.pdfAdapterAdmin = pdfAdapterAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().lowercase()
            val filterModel: ArrayList<ModelPDF> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].title.lowercase().contains(constraint)) {
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

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        pdfAdapterAdmin.pdfArrayList = results.values as ArrayList<ModelPDF>

        pdfAdapterAdmin.notifyDataSetChanged()
    }


}