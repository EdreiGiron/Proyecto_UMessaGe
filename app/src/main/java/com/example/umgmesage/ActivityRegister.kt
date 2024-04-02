package com.example.umgmesage

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.umgmesage.messaging.Models.User
import com.example.umgmesage.messaging.firebase.UsersCollection
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class ActivityRegister : AppCompatActivity() {

    private lateinit var tieneCuenta: TextView
    private lateinit var btnRegistrar: Button
    private lateinit var txtInputUsername: EditText
    private lateinit var txtInputEmail: EditText
    private lateinit var txtInputPassword: EditText
    private lateinit var txtInputConfirmPassword: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressDialog
    private lateinit var userCollections:UsersCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        userCollections=UsersCollection()
        txtInputEmail = findViewById(R.id.inputEmail_register)
        txtInputPassword = findViewById(R.id.inputPassword_register)
        txtInputConfirmPassword = findViewById(R.id.inputPassword_confirm)
        btnRegistrar = findViewById(R.id.btn_register)
        tieneCuenta = findViewById(R.id.txtIniciarSesion)

        btnRegistrar.setOnClickListener {
            verificarCredenciales()
        }

        tieneCuenta.setOnClickListener {
            startActivity(Intent(this@ActivityRegister, ActivityLogin::class.java))
        }

        mAuth = FirebaseAuth.getInstance()
        mProgressBar = ProgressDialog(this)
    }

    private fun verificarCredenciales() {
        val newuser:User= User()
        val email = txtInputEmail.text.toString()
        val password = txtInputPassword.text.toString()
        val confirmPass = txtInputConfirmPassword.text.toString()
        when {
            email.isEmpty() || !email.contains("@") -> showError(txtInputUsername, "Email no valido")
            password.isEmpty() || password.length < 7 -> showError(txtInputPassword, "Clave no valida minimo 7 caracteres")
            confirmPass.isEmpty() || confirmPass != password -> showError(txtInputConfirmPassword, "Clave no valida, no coincide.")
            else -> {
                mProgressBar.setTitle("Proceso de Registro")
                mProgressBar.setMessage("Registrando usuario, espere un momento")
                mProgressBar.setCanceledOnTouchOutside(false)
                mProgressBar.show()

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mProgressBar.dismiss()
                        newuser.userId="" //TODO("Agregar  uid generado en auth")
                        newuser.userEmail=email
                        newuser.userName=email.substringAfter('@').orEmpty()//TODO("Agregar nombre de usuario")
                        newuser.hasCustomIcon=false//TODO("Agregar funcionalidad de imagenes para que ingrese el path /Users/<uid>/Icon.png")
                        userCollections.insertUser(newuser)
                        val intent = Intent(this@ActivityRegister, ActivityLogin::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "No se pudo registrar", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showError(input: EditText, s: String) {
        input.error = s
        input.requestFocus()
    }
}
