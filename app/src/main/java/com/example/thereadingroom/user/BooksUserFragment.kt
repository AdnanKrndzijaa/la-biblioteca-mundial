package com.example.thereadingroom.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.thereadingroom.R
import com.example.thereadingroom.adminpannel.books.ModelPDF
import com.example.thereadingroom.databinding.FragmentBooksUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryID: String, category: String, uid: String) : BooksUserFragment{
            val fragment = BooksUserFragment()
            val args = Bundle()
            args.putString("categoryID", categoryID)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryID = ""
    private var category = ""
    private var uid = ""
    private lateinit var pdfArrayList: ArrayList<ModelPDF>
    private lateinit var adapterPDFUser: PDFUserAdapter

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            categoryID = args.getString("categoryID")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding  = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        if (category == "All") {
            loadAllBooks()
        } else if (category == "Most Viewed") {
            loadMostViewedDownloadedBooks("viewsCount")
        } else if (category == "Most Viewed") {
            loadMostViewedDownloadedBooks("downloadsCount")
        } else {
            loadCategorizedBooks()
        }

        binding.searchBar.addTextChangedListener { object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPDFUser.filter!!.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        } }

        return binding.root
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(5)
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPDF::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPDFUser = PDFUserAdapter(context!!, pdfArrayList)
                binding.books.adapter = adapterPDFUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPDF::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPDFUser = PDFUserAdapter(context!!, pdfArrayList)
                binding.books.adapter = adapterPDFUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryID").equalTo(categoryID)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPDF::class.java)
                        pdfArrayList.add(model!!)
                    }
                    adapterPDFUser = PDFUserAdapter(context!!, pdfArrayList)
                    binding.books.adapter = adapterPDFUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}