package com.example.umgmesage

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        val email:String? = intent.extras?.getString("email").orEmpty()
        val uid:String? = intent.extras?.getString("uid").orEmpty()

        mostrarCredenciales(email,uid)

    }

    private fun mostrarCredenciales(email: String?, uid: String?){

        val txtMostrarUID = findViewById<TextView>(R.id.txtMostrarUID)
        val txtMostrarCorreo = findViewById<TextView>(R.id.txtMostrarCorreo)
        txtMostrarUID.text = email
        txtMostrarCorreo.text = uid
    }

}