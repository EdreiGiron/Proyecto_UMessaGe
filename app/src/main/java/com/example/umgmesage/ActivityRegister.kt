package com.example.umgmesage

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.umgmesage.messaging.Models.User
import com.example.umgmesage.messaging.firebase.UsersCollection
import com.google.firebase.auth.FirebaseAuth

class ActivityRegister : AppCompatActivity() {

    private lateinit var tieneCuenta: TextView
    private lateinit var btnRegistrar: Button
    private lateinit var txtInputEmail: EditText
    private lateinit var txtInputPassword: EditText
    private lateinit var txtInputUserName: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressBar: ProgressDialog
    private lateinit var userCollections:UsersCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        userCollections=UsersCollection()
        txtInputEmail = findViewById(R.id.inputEmail_register)
        txtInputPassword = findViewById(R.id.inputPassword_register)
        txtInputUserName = findViewById(R.id.inputUserName)
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
//Se crea metodo para que permita solo registrar correos con dominio de "@miumg.edu.gt"

    //Adicional muestra una alerta que indica que solo pernmite correos umg
    private fun verificarCredenciales() {
        val newuser:User= User()
        val email = txtInputEmail.text.toString()
        val password = txtInputPassword.text.toString()
        val userName = txtInputUserName.text.toString()

        //esta variable allowedDomain se creo para que permita solo correos con dominio umg, se restringe cualquier dominio como gmail etc
        val allowedDomain = "@miumg.edu.gt"
        when {
            email.isEmpty() -> showError(txtInputEmail, "Por favor, ingrese su correo electrónico")
            password.isEmpty() || password.length < 7 -> showError(txtInputPassword, "Contraseña invalida, el minimo es de 7 caracteres")

            //muestra alerta de que solo permite registro con correos UMG
            !email.endsWith(allowedDomain) -> showError(txtInputEmail, "Solo se permiten correos electrónicos con dominio $allowedDomain")
            userName.isEmpty() || userName.length > 20 -> showError(txtInputUserName, "El nombre de usuario debe ser menor a 20 caracteres.")
            else -> {
                mProgressBar.setTitle("Proceso de Registro")
                mProgressBar.setMessage("Registrando usuario, espere un momento")
                mProgressBar.setCanceledOnTouchOutside(false)
                mProgressBar.show()

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val auth = mAuth
                        //Obteniendo el usuario registrado
                        val user = auth.currentUser;
                        mProgressBar.dismiss()
                        //Guardando el Id del usuario registrado
                        newuser.userId= user?.uid
                        newuser.userEmail=email
                        newuser.userName= userName //email.substringAfter('@').orEmpty()
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
