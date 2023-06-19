package com.example.thereadingroom

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thereadingroom.databinding.RowRecommendationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class RecommendationAdapter:RecyclerView.Adapter<RecommendationAdapter.HolderRecommendation> {

    val context: Context
    val recommendationArrayList: ArrayList<ModelRecommendation>

    private lateinit var binding: RowRecommendationBinding

    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, recommendationArrayList: ArrayList<ModelRecommendation>) {
        this.context = context
        this.recommendationArrayList = recommendationArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderRecommendation(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = binding.accountImage
        val name = binding.accountName
        val date = binding.recommendationDate
        val recommendation = binding.recommendationContent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecommendation {
        binding = RowRecommendationBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderRecommendation(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecommendation, position: Int) {
        val model = recommendationArrayList[position]

        val id = model.id
        val uid = model.uid
        val bookID = model.bookID
        val recommendation = model.comment
        val timestamp = model.timestamp

        val date = MyApp.formatTimeStamp(timestamp.toLong())

        holder.date.text = date
        holder.recommendation.text = recommendation

        loadUserDetails(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteRecDialog(model, holder)
            }
        }
    }

    private fun deleteRecDialog(model: ModelRecommendation, holder: RecommendationAdapter.HolderRecommendation) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete recommendation")
            .setMessage("Are you sure you want to delete your recommendation?")
            .setPositiveButton("Delete") { d, e->
                val bookID = model.bookID
                val recommendationID = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookID).child("Comments").child(recommendationID)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e->
                        Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel") {d, e->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelRecommendation, holder: RecommendationAdapter.HolderRecommendation) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("first_name").value}" + " " + "${snapshot.child("last_name").value}"
                    val profilePicture = "${snapshot.child("profilePicture").value}"

                    holder.name.text = name
                    try {
                        Glide.with(context)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_green)
                            .into(holder.image)
                    } catch (e: Exception) {
                        holder.image.setImageResource(R.drawable.account_green)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int {
        return recommendationArrayList.size
    }

}