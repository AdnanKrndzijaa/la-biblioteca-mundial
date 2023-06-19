package com.example.thereadingroom

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.thereadingroom.databinding.ActivityEditProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var photoUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.profilePicture.setOnClickListener {
            showImageAttachMenu()
        }
        binding.update.setOnClickListener {
            validateData()
        }

    }

    private var fname = ""
    private var lname = ""

    private fun validateData() {
        fname = binding.userFirstName.text.toString()
        lname = binding.userLastName.text.toString()

        if (fname.isEmpty()) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
        } else if (lname.isEmpty()) {
            Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
        } else {
            if (photoUri == null) {
                updateProfile("")
            } else {
                uploadProfileImg()
            }
        }
    }

    private fun uploadProfileImg() {
        progressDialog.setMessage("Uploading profile photo...")
        progressDialog.show()

        val filePathAndName = "ProfilePictures/"+firebaseAuth.uid
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(photoUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUri = "${uriTask.result}"
                updateProfile(uploadedImageUri)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload photo due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUri: String) {
        progressDialog.setMessage("Updating profile...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["first_name"] = "$fname"
        hashMap["last_name"] = "$lname"

        if (photoUri != null) {
            hashMap["profilePicture"] = uploadedImageUri
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update photo due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = "${snapshot.child("first_name").value}"
                    val lastName = "${snapshot.child("last_name").value}"
                    val profilePicture = "${snapshot.child("profilePicture").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    val formattedDate = MyApp.formatTimeStamp(timestamp.toLong())
                    binding.userFirstName.setText(firstName)
                    binding.userLastName.setText(lastName)

                    try {
                        Glide.with(this@EditProfile)
                            .load(profilePicture)
                            .placeholder(R.drawable.account_white)
                            .into(binding.profilePicture)
                    } catch (e: Exception) {

                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showImageAttachMenu() {
        val popUpMenu = PopupMenu(this, binding.profilePicture)
        popUpMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popUpMenu.menu.add(Menu.NONE, 0, 0, "Gallery")
        popUpMenu.show()

        popUpMenu.setOnMenuItemClickListener { item->
            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }

            true
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                binding.profilePicture.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                photoUri = data!!.data

                binding.profilePicture.setImageURI(photoUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}