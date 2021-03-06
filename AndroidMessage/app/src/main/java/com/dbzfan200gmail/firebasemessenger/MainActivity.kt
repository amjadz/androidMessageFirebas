package com.dbzfan200gmail.firebasemessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button.setOnClickListener {
            registerButton()

        }


        textView.setOnClickListener {
            loginIntent()


        }

        add_image.setOnClickListener {
            addPhoto()

        }
    }

    private fun registerButton() {
        val email = email.text.toString()
        val password = password.text.toString()
        val username = name.text.toString()

        if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please Enter Email and Password", Toast.LENGTH_LONG).show()
        } else {
            firebaseCreateUser(email, password)

        }

    }

    private fun loginIntent(){

        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)



    }

    private fun firebaseCreateUser(email: String, password: String){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful)  return@addOnCompleteListener
                Toast.makeText(this, "User was created", Toast.LENGTH_LONG).show()
                uploadImageToFireBase()
            } .addOnFailureListener{
                Toast.makeText(this, "User was not able to be created", Toast.LENGTH_LONG).show()
            }
    }

    var selectedPhotoUri: Uri? = null

    private fun uploadImageToFireBase(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()

                    saveUserToDatabase(it.toString())
                }
            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
       val uid = FirebaseAuth.getInstance().uid ?: ""

       val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

       var user = UserModel(uid, password.text.toString(), email.text.toString(), name.text.toString(), profileImageUrl)

       ref.setValue(user)
           .addOnSuccessListener {
               val intent = Intent(this, LatestestMessages::class.java)
               intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_NEW_TASK)

               startActivity(intent)

           }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            photo_image.setImageBitmap(bitmap)

            add_image.alpha = 0f
        }

    }

    private fun addPhoto(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, 0)


    }

}
