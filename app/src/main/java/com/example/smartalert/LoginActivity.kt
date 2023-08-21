package com.example.smartalert

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private var email: String = ""
    private var password: String = ""
    val Req_Code:Int=123
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)


        val email = findViewById<EditText>(R.id.emailText)
        val password = findViewById<EditText>(R.id.passwordText)
        val loginBtn = findViewById<Button>(R.id.continueBtn)
        val signUpBtn = findViewById<Button>(R.id.createAccount)

        val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
        // Apply the appropriate theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        //Sign in with Google :
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_client))
            .requestEmail()
            .build()
        // getting the value of gso inside the GoogleSigninClient
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        val googleBtn = findViewById<ImageButton>(R.id.googleBtn)
        googleBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            signInGoogle()
        }
        //Log in with email/ password
        loginBtn.setOnClickListener {
            animationView.visibility = View.VISIBLE
            Log.d("Login", "logging in with email")
            val emailText = email.text.toString()
            val passwordText = password.text.toString()


            Log.d("Login", "Email: $emailText and Password: $passwordText")
            //spinner.visibility = View.VISIBLE
            logIn(email.text.toString(), password.text.toString())
        }

        //Go to Register Page
        signUpBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signInGoogle(){
        //spinner.visibility = View.VISIBLE
        Log.d("Login", "Logging with Google")
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,Req_Code)
    }
    // onActivityResult() function : this is where we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Req_Code){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
        else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Check if the user granted notification permission
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val areNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                notificationManager.areBubblesAllowed()
            } else {
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            }

            if (areNotificationsEnabled) {
                // Notification permission granted
            } else {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                    }
                }
            }
        }
    }
    // handleResult() function -  this is where we update the UI after Google signin takes place
    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException){
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
            val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
            animationView.visibility = View.GONE
        }
    }
    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
                animationView.visibility = View.GONE
                startActivity(intent)
                Log.d("Logging in", "Successfully logged in with Google")
            }
        }
    }
    private fun logIn(email:String, password: String) {
        if(email != "" && password != ""){
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
                    animationView.visibility = View.GONE
                    startActivity(intent)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                val animationView = findViewById<LottieAnimationView>(R.id.lottie_animation_view)
                animationView.visibility = View.GONE
            }
        }else{
            showAlert("Credentials", "Please enter your credentials")
        }


    }
    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}