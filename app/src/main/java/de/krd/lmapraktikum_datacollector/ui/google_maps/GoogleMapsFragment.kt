package de.krd.lmapraktikum_datacollector.ui.google_maps

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoogleMapsFragment : Fragment(), OnMapReadyCallback, OnCameraMoveStartedListener {
    private val model: GlobalModel by activityViewModels()
    private var polyline: Polyline? = null
    private var follow = true
    private var followJob: Job? = null;
    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_maps, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val view = requireView()
        mapView = view.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

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
            val latLngs: List<LatLng> = locations.map { LatLng(it.latitude, it.longitude) };

            var polylineOptions = PolylineOptions()
            polylineOptions.addAll(latLngs)
            polylineOptions.width(5f)
            polylineOptions.color(Color.BLUE)
            polylineOptions.geodesic(true)
            polyline = map.addPolyline(polylineOptions)

            /*
         * Move camera smoothly to new location.
         */
            if (follow) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLngs.last(),
                        model.settings.googleMaps.value!!.zoomFactor
                    )
                )
            }

        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == OnCameraMoveStartedListener.REASON_GESTURE) {
            follow = false
            if (model.settings.googleMaps.value!!.followEnabled) {
                followJob?.cancel()
                followJob = GlobalScope.launch {
                    delay(10000)
                    follow = true
                }
            }
        } else if (reason == OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
        } else if (reason == OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
        }
    }

}