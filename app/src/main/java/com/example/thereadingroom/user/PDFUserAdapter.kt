package com.example.thereadingroom.user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.thereadingroom.MyApp
import com.example.thereadingroom.adminpannel.books.BookDetails
import com.example.thereadingroom.adminpannel.books.ModelPDF
import com.example.thereadingroom.databinding.UserRowBinding

class PDFUserAdapter : RecyclerView.Adapter<PDFUserAdapter.HolderPDFUser>, Filterable{
    private lateinit var binding: UserRowBinding
    private var context: Context
    public var pdfArrayList: ArrayList<ModelPDF>
    private var filterList: ArrayList<ModelPDF>

    private var filter: FilterBookUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPDF>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    inner class HolderPDFUser(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image = binding.imageCategoryBooks
        val progressBar = binding.progressBar
        val title = binding.titleCategoryBooks
        val author = binding.authorCategoryBooks
        //val description = binding.descriptionCategoryBooks
        val category = binding.categoryCategoryBooks
        val date = binding.dateCategoryBooks

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPDFUser {
        binding = UserRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPDFUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPDFUser, position: Int) {
        val model = pdfArrayList[position]
        val pdfID = model.id
        val categoryID = model.categoryID
        val title = model.title
        val author = model.author
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        val formattedDate = MyApp.formatTimeStamp(timestamp)
        holder.title.text = title
        holder.author.text = author
        //holder.description.text = description
        holder.date.text = formattedDate

        MyApp.loadCategory(categoryID, holder.category)
        MyApp.loadPDFFromUrl(pdfUrl, title, holder.image, holder.progressBar, null)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetails::class.java)
            intent.putExtra("bookID", pdfID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterBookUser(filterList, this)
        }
        return filter as FilterBookUser
    }
}