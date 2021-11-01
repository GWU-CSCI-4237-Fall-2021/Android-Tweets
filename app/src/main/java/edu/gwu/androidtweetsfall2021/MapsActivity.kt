package edu.gwu.androidtweetsfall2021

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import edu.gwu.androidtweetsfall2021.databinding.ActivityMapsBinding
import org.jetbrains.anko.doAsync

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var confirm: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var currentAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sets up the XML Layout using using ViewBinding
        // https://developer.android.com/topic/libraries/view-binding
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val currentUser: FirebaseUser = firebaseAuth.currentUser!!
        title = getString(R.string.maps_title, currentUser.email)


        confirm = findViewById(R.id.confirm)
        confirm.setOnClickListener {
            firebaseAnalytics.logEvent("confirm_clicked", null)
            val address = currentAddress
            if (address != null) {
                val intent = Intent(this, TweetsActivity::class.java)
                intent.putExtra("address", address)
                startActivity(intent)
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Trigger the map to start loading
        mapFragment.getMapAsync(this)
    }

    fun updateCurrentAddress(address: Address) {
        currentAddress = address
        confirm.text = address.getAddressLine(0)

        confirm.icon = getDrawable(R.drawable.ic_check)
        confirm.setBackgroundColor(getColor(R.color.buttonGreen))
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        googleMap.setOnMapLongClickListener { coords: LatLng ->
            googleMap.clear()
            firebaseAnalytics.logEvent("map_long_press", null)

            // Geocoding should be done on a background thread - it involves networking
            // and has the potential to cause the app to freeze (Application Not Responding error)
            // if done on the UI Thread and it takes too long.
            doAsync {
                val geocoder: Geocoder = Geocoder(this@MapsActivity)

                // In Kotlin, you can assign the result of a try-catch block. Both the "try" and
                // "catch" clauses need to yield a valid value to assign.
                val results: List<Address> = try {
                    geocoder.getFromLocation(coords.latitude, coords.longitude, 10).also { results ->
                        firebaseAnalytics.logEvent("geocoding_success", Bundle().apply {
                            putString("count", "" + results.size)
                        })
                    }
                } catch (exception: Exception) {
                    // Uses the error logger to print the error
                    Log.e("MapsActivity", "Geocoding failed", exception)

                    // Uses System.out.println to print the error
                    exception.printStackTrace()

                    firebaseAnalytics.logEvent("geocoding_failed", null)
                    Firebase.crashlytics.recordException(exception)
                    listOf()
                }

                // Move back to the UI Thread now that we have some results to show.
                // The UI can only be updated from the UI Thread.
                runOnUiThread {
                    if (results.isNotEmpty()) {
                        // Potentially, we could show all results to the user to choose from,
                        // but for our usage it's sufficient enough to just use the first result.
                        // The Geocoder's first result is often the "best" one in terms of its accuracy / confidence.
                        val firstResult: Address = results[0]
                        val postalAddress: String = firstResult.getAddressLine(0)

                        Log.d("MapsActivity", "First result: $postalAddress")

                        googleMap.addMarker(
                            MarkerOptions().position(coords).title(postalAddress)
                        )

                        // Add a map marker where the user tapped and pan the camera over
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 10.0f))

                        updateCurrentAddress(firstResult)
                    } else {
                        Log.d("MapsActivity", "No results from geocoder!")

                        val toast = Toast.makeText(
                            this@MapsActivity,
                            getString(R.string.geocoder_no_results),
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                }
            }
        }
    }
}