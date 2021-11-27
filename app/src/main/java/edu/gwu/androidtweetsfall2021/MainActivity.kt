package edu.gwu.androidtweetsfall2021

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // For an explaination of why lateinit var is needed, see:
    // https://docs.google.com/presentation/d/1icewQjn-fkd-wTepzRoqXOjaKWtGUrx0o0Us2anJz3w/edit#slide=id.g615c45607e_0_156
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var signUp: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var shakeManager: ShakeManager

    // onCreate is called the first time the Activity is to be shown to the user, so it a good spot
    // to put initialization logic.
    // https://developer.android.com/guide/components/activities/activity-lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tells Android which layout file should be used for this screen.
        setContentView(R.layout.activity_main)

        shakeManager = ShakeManager(this)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Equivalent of a System.out.println (Android has different logging levels to organize logs -- .d is for DEBUG)
        // First parameter = the "tag" allows you to find related logging statements easier (e.g. all logs in the MainActivity)
        // Second parameter = the actual thing you want to log
        Log.d("MainActivity", "onCreate called!")

        val preferences: SharedPreferences = getSharedPreferences("android-tweets", Context.MODE_PRIVATE)


        // The IDs we are using here should match what was set in the "id" field for our views
        // in our XML layout (which was specified by setContentView).
        // Android will "search" the UI for the elements with the matching IDs to bind to our variables.
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        signUp = findViewById(R.id.signUp)
        progressBar = findViewById(R.id.progressBar)

        // Kotlin shorthand for login.setEnabled(false).
        // If the getter / setter is unambiguous, Kotlin lets you use the property-style syntax
        login.isEnabled = false
        signUp.isEnabled = false

        // Restore the saved username from SharedPreferences and display it to the user when the screen loads.
        // Default to the empty string if there is no saved username.
        val savedUsername = preferences.getString("USERNAME", "")
        username.setText(savedUsername)

        // Using a lambda to implement a View.OnClickListener interface. We can do this because
        // an OnClickListener is an interface that only requires *one* function.
        login.setOnClickListener {
            firebaseAnalytics.logEvent("login_clicked", null)

            // Save the username to SharedPreferences
            val inputtedUsername = username.text.toString()
            val inputtedPassword = password.text.toString()
            showLoading()

            firebaseAuth
                .signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    hideLoading()

                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("login_success", null)

                        val currentUser: FirebaseUser = firebaseAuth.currentUser!!
                        Toast.makeText(
                            this,
                            "Logged in as: ${currentUser.email}",
                            Toast.LENGTH_LONG
                        ).show()

                        val editor = preferences.edit()
                        editor.putString("USERNAME", inputtedUsername)
                        editor.apply()


                        // An Intent is used to start a new Activity.
                        // 1st param == a "Context" which is a reference point into the Android system. All Activities are Contexts by inheritance.
                        // 2nd param == the Class-type of the Activity you want to navigate to.
                        val intent: Intent = Intent(this, MapsActivity::class.java)

                        // An Intent can also be used like a Map (key-value pairs) to pass data between Activities.
                        // intent.putExtra("LOCATION", "Washington")

                        // "Executes" our Intent to start a new Activity
                        startActivity(intent)
                    } else {
                        val bundle = Bundle()
                        val exception: Exception? = task.exception

                        if (exception != null) {
                            Firebase.crashlytics.recordException(exception)
                        }

                        when (exception) {
                            is FirebaseAuthInvalidUserException -> {
                                bundle.putString("error_type", "invalid_user")
                                firebaseAnalytics.logEvent("login_failed", bundle)
                                Toast.makeText(
                                    this,
                                    R.string.login_failure_doesnt_exist,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                bundle.putString("error_type", "invalid_credentials")
                                firebaseAnalytics.logEvent("login_failed", bundle)
                                Toast.makeText(
                                    this,
                                    R.string.login_failure_wrong_credentials,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                bundle.putString("error_type", "generic")
                                firebaseAnalytics.logEvent("login_failed", bundle)
                                Toast.makeText(
                                    this,
                                    getString(R.string.login_failure_generic, exception),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }

        signUp.setOnClickListener {
            val inputtedUsername: String = username.text.toString()
            val inputtedPassword: String = password.text.toString()
            showLoading()

            firebaseAnalytics.logEvent("signup_clicked", null)

            firebaseAuth
                .createUserWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    hideLoading()

                    if (task.isSuccessful) {
                        firebaseAnalytics.logEvent("signup_success", null)
                        val currentUser: FirebaseUser = firebaseAuth.currentUser!!

                        Toast.makeText(
                            this,
                            "Registered successfully as: ${currentUser.email}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val exception: Exception? = task.exception
                        val bundle = Bundle()

                        if (exception != null) {
                            Firebase.crashlytics.recordException(exception)
                        }

                        when (exception) {
                            is FirebaseAuthWeakPasswordException -> {
                                bundle.putString("error_type", "weak_password")
                                firebaseAnalytics.logEvent("signup_failed", bundle)
                                Toast.makeText(
                                    this,
                                    R.string.signup_failure_weak_password,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is FirebaseAuthUserCollisionException -> {
                                bundle.putString("error_type", "user_collision")
                                firebaseAnalytics.logEvent("signup_failed", bundle)
                                Toast.makeText(
                                    this,
                                    R.string.signup_failure_already_exists,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                bundle.putString("error_type", "invalid_credentials")
                                firebaseAnalytics.logEvent("signup_failed", bundle)
                                Toast.makeText(
                                    this,
                                    R.string.signup_failure_invalid_format,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                bundle.putString("error_type", "generic")
                                firebaseAnalytics.logEvent("signup_failed", bundle)
                                Toast.makeText(
                                    this,
                                    getString(R.string.signup_failure_generic, exception),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }

        // Using the same TextWatcher instance for both EditTexts so the same block of code runs on each character.
        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
    }

    override fun onResume() {
        super.onResume()
        shakeManager.startDetectingShakes {
            // ...
            Log.d("MainActivity", "Shake occurred!")
        }
    }

    override fun onPause() {
        shakeManager.stopDetectingShakes()
        super.onPause()
    }

    // Displays the loading indicator and disables user input
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        login.isEnabled = false
        signUp.isEnabled = false
        username.isEnabled = false
        password.isEnabled = false
    }

    // Hides the loading indicator and enables user input
    private fun hideLoading() {
        progressBar.visibility = View.INVISIBLE
        login.isEnabled = true
        signUp.isEnabled = true
        username.isEnabled = true
        password.isEnabled = true
    }


    // Another example of explicitly implementing an interface (TextWatcher). We cannot use
    // a lambda in this case since there are multiple functions we need to implement.
    //
    // We're defining an "anonymous class" here using the `object` keyword (basically creating
    // a new, dedicated object to implement a TextWatcher for this variable assignment).
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Kotlin shorthand for username.getText().toString()
            // .toString() is needed because getText() returns an Editable (basically a char array).
            val inputtedUsername: String = username.text.toString()
            val inputtedPassword: String = password.text.toString()
            val enableButton: Boolean = inputtedUsername.isNotBlank() && inputtedPassword.isNotBlank()

            // Kotlin shorthand for login.setEnabled(enableButton)
            login.isEnabled = enableButton
            signUp.isEnabled = enableButton
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}
