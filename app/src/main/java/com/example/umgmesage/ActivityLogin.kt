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

class ActivityLogin : AppCompatActivity() {

    private lateinit var lblCrearCuenta: TextView
    private lateinit var txtInputEmail: EditText
    private lateinit var txtInputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var lblOlvidasteContra: TextView
    private lateinit var mProgressBar: ProgressDialog


    //Funcion principal

    //La funcion de esta clase es que pueda realizar el usiario su inicio de sesion, media vez tenga correo registrado
    //en la base de datos de Firebase, se creo metodo para login de usuario
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtInputEmail = findViewById(R.id.inputEmail)
        txtInputPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnlogin)
        lblCrearCuenta = findViewById(R.id.txtNotieneCuenta)
        lblOlvidasteContra = findViewById(R.id.forgotPassword)

        lblCrearCuenta.setOnClickListener {
            startActivity(Intent(this@ActivityLogin, ActivityRegister::class.java))
        }
        btnLogin.setOnClickListener {
            verificarCredenciales()
        }
        lblOlvidasteContra.setOnClickListener {
            startActivity(Intent(this@ActivityLogin, ActivityPassw::class.java))
        }
        mProgressBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

    }
//Le permite al usuario poder ingresar sus credenciales como correo y contraseña
    private fun verificarCredenciales() {
        val email = txtInputEmail.text.toString()
        val password = txtInputPassword.text.toString()

//condicion para que ingrese correo con @ y que la contraseña sea mayor a 7 digitos
        when {
            email.isEmpty() || !email.contains("@") -> showError(
                txtInputEmail,
                "Por favor ingrese su correo electrónico"
            )

            password.isEmpty() || password.length < 7 -> showError(
                txtInputPassword,
                "Password invalida"
            )

            //Si los datos estan correctos, muestra un progressbar que esta iniciando sesion
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
                        val intent = Intent(this@ActivityLogin, ChatsActivity::class.java)
                        intent.putExtra("email", email)
                        if (user != null) {
                            intent.putExtra("userId", user.uid)
                        }

                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        //si el usuario esta colocando mal los datos, muestra alerta que correo y password erroneos, los debe de corregir
                    } else {

                        Toast.makeText(
                            applicationContext,
                            "No se pudo iniciar sesion, verifique los datos de correo/password",
                            Toast.LENGTH_LONG
                        ).show()
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
