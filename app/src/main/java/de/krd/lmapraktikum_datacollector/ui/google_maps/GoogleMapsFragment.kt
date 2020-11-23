package de.krd.lmapraktikum_datacollector.ui.google_maps

import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoogleMapsFragment : Fragment(), OnMapReadyCallback, OnCameraMoveStartedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private val model: GlobalModel by activityViewModels()
    private var polyline: Polyline? = null
    private var follow = true
    private var animationInProgress = false
    private var followJob: Job? = null;
    private var zoomFactor = 0.0f
    private var keepFollowing = true
    private var followingDelayTimeMs = 10000L
    private var showRoute = false;
    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_maps, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        preferences.registerOnSharedPreferenceChangeListener(this)

        loadPreferences()

        val view = requireView()
        mapView = view.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        animationInProgress = false

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /*
     * Assign GoogleMaps Object if fragment is ready
     */
    override fun onMapReady(googleMap: GoogleMap?) {
        this.map = googleMap!!

        model.data.locations.observe(viewLifecycleOwner, Observer {
            onLocationChange(it)
        })

        map.setOnCameraMoveStartedListener(this)
    }

    private fun onLocationChange(locations: MutableList<Location>) {
        polyline?.remove()
        if (!locations.isEmpty()) {

            if (showRoute) {
                val latLngs: List<LatLng> = locations.map { LatLng(it.latitude, it.longitude) }
                var polylineOptions = PolylineOptions()
                polylineOptions.addAll(latLngs)
                polylineOptions.width(3f)
                polylineOptions.color(Color.BLUE)
                polylineOptions.geodesic(true)
                polyline = map.addPolyline(polylineOptions)
            }

            var circleOptions = CircleOptions()
            circleOptions.radius(1.0)

            locations.forEach {
                when (it.provider.toUpperCase()) {
                    "GPS" -> {
                        circleOptions.strokeColor(Color.RED)
                    }
                    "NETWORK" -> {
                        circleOptions.strokeColor(Color.GREEN)
                    }
                }
                circleOptions.center(LatLng(it.latitude, it.longitude))
                map.addCircle(circleOptions)
            }

            /*
         * Move camera smoothly to new location.
         */
            if (follow && !animationInProgress) {
                val lastLocation = locations.last();
                animationInProgress = true
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLocation.latitude, lastLocation.longitude),
                        zoomFactor
                    ),
                    object : CancelableCallback {
                        override fun onFinish() {
                            animationInProgress = false
                        }

                        override fun onCancel() {
                            animationInProgress = false
                        }
                    }
                )
            }

        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
            follow = false
            if (keepFollowing) {
                followJob?.cancel()
                followJob = GlobalScope.launch {
                    delay(followingDelayTimeMs)
                    follow = true
                }
            }
        } else if (reason == OnCameraMoveStartedListener
                .REASON_API_ANIMATION
        ) {
        } else if (reason == OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION
        ) {
        }
    }

    private fun loadPreferences() {
        val context = requireContext()
        zoomFactor = PreferenceHelper.getFloat(
            context,
            preferences,
            R.string.setting_google_maps_zoom_factor
        )
        keepFollowing = PreferenceHelper.getBoolean(
            context,
            preferences,
            R.string.setting_google_maps_follow_location
        )
        followingDelayTimeMs = PreferenceHelper.getLong(
            context,
            preferences,
            R.string.setting_google_maps_follow_timeout
        )
        showRoute = PreferenceHelper.getBoolean(
            context,
            preferences,
            R.string.setting_google_maps_enable_polyline
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val context = requireContext()
        when (key) {
            getString(R.string.setting_google_maps_zoom_factor),
            getString(R.string.setting_google_maps_follow_timeout),
            getString(R.string.setting_google_maps_enable_polyline)-> {
                loadPreferences()
            }
            getString(R.string.setting_google_maps_follow_location) -> {
                loadPreferences()
                follow = keepFollowing
            }
        }
    }

}