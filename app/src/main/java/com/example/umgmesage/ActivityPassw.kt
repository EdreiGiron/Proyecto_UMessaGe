package com.example.umgmesage

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ActivityPassw : AppCompatActivity() {

    private lateinit var txtInputEmail: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressDialog
    private lateinit var btnRecuperar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_passw)

        btnRecuperar = findViewById(R.id.btnRecuperar)
        txtInputEmail = findViewById(R.id.inputEmail)

        btnRecuperar.setOnClickListener {
            validate()
        }

        mAuth = FirebaseAuth.getInstance()
        mProgressBar = ProgressDialog(this)
    }

    //metodo para que ingrese el correo, siempre y cuando sea correo con dominio "@miumg.edu.gt"
    private fun validate() {
        val allowedDomain = "@miumg.edu.gt"
        val email = txtInputEmail.text.toString()

        when {
            email.isEmpty() -> showError(txtInputEmail, "Por favor, ingrese su correo electrónico")
            !email.endsWith(allowedDomain) -> showError(txtInputEmail, "Solo se permiten correos electrónicos con dominio $allowedDomain")
            else -> {
                mProgressBar.setTitle("Proceso de envío")
                mProgressBar.setMessage("Enviando correo")
                mProgressBar.setCanceledOnTouchOutside(false)
                mProgressBar.show()
                sendEmail(email)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ActivityPassw, ActivityLogin::class.java))
    }

    //metodo para poder enviar el correo con la opcion de reinicio de password a los correos registrados en Firebase
    private fun sendEmail(email: String) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@ActivityPassw, ActivityLogin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Correo no enviado", Toast.LENGTH_LONG).show()
            }
            mProgressBar.dismiss()
        }
    }

    private fun showError(input: EditText, s: String) {
        input.error = s
        input.requestFocus()
    }
}