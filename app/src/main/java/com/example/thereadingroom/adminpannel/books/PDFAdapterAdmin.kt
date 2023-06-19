package com.example.thereadingroom.adminpannel.books

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.thereadingroom.MyApp
import com.example.thereadingroom.databinding.CategoryRowBooksBinding

class PDFAdapterAdmin : RecyclerView.Adapter<PDFAdapterAdmin.HolderPDFAdmin>, Filterable{
    private lateinit var binding: CategoryRowBooksBinding
    private val context: Context
    private val filterList: ArrayList<ModelPDF>

    private var filter: PDFFilterAdmin? = null

    public var pdfArrayList: ArrayList<ModelPDF>

    constructor(context: Context, pdfArrayList: ArrayList<ModelPDF>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPDFAdmin {
        binding = CategoryRowBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPDFAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPDFAdmin, position: Int) {
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

        holder.more.setOnClickListener{
            optionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetails::class.java)
            intent.putExtra("bookID", pdfID)
            context.startActivity(intent)
        }
    }

    private fun optionsDialog(model: ModelPDF, holder: PDFAdapterAdmin.HolderPDFAdmin) {
        val bookID = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        val options = arrayOf("Edit a book", "Delete a book")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select option")
            .setItems(options){ dialog, position ->
                if (position == 0) {
                    val intent = Intent(context, EditBook::class.java)
                    intent.putExtra("bookID", bookID)
                    context.startActivity(intent)
                } else if (position == 1) {

                    MyApp.deleteBook(context, bookID, bookUrl, bookTitle)
                }
            }
            .show()

    }


    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = PDFFilterAdmin(filterList, this)
        }
        return filter as PDFFilterAdmin
    }

    inner class HolderPDFAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image = binding.imageCategoryBooks
        val progressBar = binding.progressBar
        val title = binding.titleCategoryBooks
        val author = binding.authorCategoryBooks
        //val description = binding.descriptionCategoryBooks
        val category = binding.categoryCategoryBooks
        val date = binding.dateCategoryBooks
        val more = binding.deleteEditBook

    }
}