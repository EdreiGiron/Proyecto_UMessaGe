package com.example.umgmesage

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActivityLogin : AppCompatActivity() {

    private lateinit var lblCrearCuenta: TextView
    private lateinit var txtInputEmail: EditText
    private lateinit var txtInputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtInputEmail = findViewById(R.id.inputEmail)
        txtInputPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnlogin)
        lblCrearCuenta = findViewById(R.id.txtNotieneCuenta)

        lblCrearCuenta.setOnClickListener {
            startActivity(Intent(this@ActivityLogin, ActivityRegister::class.java))
        }
        btnLogin.setOnClickListener {
            verificarCredenciales()
        }

        mProgressBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

    }

    private fun verificarCredenciales() {
        val email = txtInputEmail.text.toString()
        val password = txtInputPassword.text.toString()
        when {
            email.isEmpty() || !email.contains("@") -> showError(txtInputEmail, "Email no valido. valide bien por favor")
            password.isEmpty() || password.length < 7 -> showError(txtInputPassword, "Password invalida")
            else -> {
                mProgressBar.setTitle("Login")
                mProgressBar.setMessage("Iniciando sesión, espere un momento..")
                mProgressBar.setCanceledOnTouchOutside(false)
                mProgressBar.show()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val auth = mAuth;
                        val user = auth.currentUser;
                        mProgressBar.dismiss()
                        val homeIntent = Intent(this@ActivityLogin, ChatsActivity::class.java)
                        homeIntent.putExtra("userErmail", email)
                            if (user != null) {
                                homeIntent.putExtra("userId", user.uid.toString())
                            }

                        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(homeIntent)
                    } else {
                        Toast.makeText(applicationContext, "No se pudo iniciar sesion, verifique los datos de correo/password", Toast.LENGTH_LONG).show()
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
