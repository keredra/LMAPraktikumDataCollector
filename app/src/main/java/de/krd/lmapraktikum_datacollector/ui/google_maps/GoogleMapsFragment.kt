package de.krd.lmapraktikum_datacollector.ui.google_maps

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.data.PositionEvaluationData
import de.krd.lmapraktikum_datacollector.utils.PreferenceHelper
import kotlinx.android.synthetic.main.route_control.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception


class GoogleMapsFragment : Fragment(), OnMapReadyCallback, OnCameraMoveStartedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private val model: GlobalModel by activityViewModels()
    private var polyline: Polyline? = null
    private var follow = true
    private var animationInProgress = false
    private var followJob: Job? = null
    private var zoomFactor = 0.0f
    private var keepFollowing = true
    private var followingDelayTimeMs = 10000L
    private var showRoute = false
    private var showAccuracy = false

    private var routeStarted = false
    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var preferences: SharedPreferences

    private val listOfCircles = mutableListOf<Circle>()
    private val listOfMarker = mutableListOf<Marker>()

    private lateinit var locationsObserver: Observer<MutableList<LocationData>>
    private lateinit var routeObserver: Observer<MutableList<PositionEvaluationData>>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_google_maps, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        loadPreferences()

        val view = requireView()
        mapView = view.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        animationInProgress = false

        locationsObserver = Observer { onLocationChange(it) }
        routeObserver = Observer { onPositionEvaluationRouteChange(it) }

        btnRouteStartNext.setOnClickListener { onStartNextButtonClick() }
        btnReset.setOnClickListener { onResetButtonClick() }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        preferences.registerOnSharedPreferenceChangeListener(this)
        model.data.locations.observe(viewLifecycleOwner, locationsObserver)
        model.data.route.observe(viewLifecycleOwner, routeObserver)
    }

    override fun onPause() {
        mapView.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        model.data.locations.removeObserver(locationsObserver)
        model.data.route.removeObserver(routeObserver)
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

        this.map.setOnMapClickListener(OnMapClickListener { position -> // TODO Auto-generated method stub
            if (!routeStarted)
                model.data.route.add(PositionEvaluationData(position, 0L, 0L))
        })

        map.setOnCameraMoveStartedListener(this)
    }

    private fun onPositionEvaluationRouteChange(route: MutableList<PositionEvaluationData>) {
        var i = 0
        listOfMarker.forEach { it.remove() }
        listOfMarker.clear()
        var actual = routeStarted
        route.forEach { positionEvaluationData ->
            var drawableId = R.drawable.ic_location_on_red
            if (actual) {
                drawableId = R.drawable.ic_location_on_yellow

                if (positionEvaluationData.tsArrival == 0L) {
                    actual = false
                } else if (positionEvaluationData.tsDepature == 0L) {
                    actual = false
                    drawableId = R.drawable.ic_location_on_green
                } else {
                    drawableId = R.drawable.ic_location_on_green
                }
            }

            listOfMarker.add(
                    map.addMarker(
                            getRouteLocationMarkerOptions(
                                    positionEvaluationData.latLng, "" + (++i), drawableId)))
        }

        clRouteControl.visibility = if (route.size == 0) View.INVISIBLE else View.VISIBLE


    }
    private fun getRouteLocationMarkerOptions(latLng: LatLng, label: String, drawableId: Int) : MarkerOptions {
        val rActivity = requireActivity()
        val view = (rActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.route_marker, null)
        view.findViewById<TextView>(R.id.num_txt).text = label
        view.findViewById<ImageView>(R.id.marker_tag).setImageDrawable(rActivity.getDrawable(drawableId))
        return MarkerOptions().position(latLng).title(latLng.toString()).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(rActivity, view)))
    }

    private fun onLocationChange(locations: MutableList<LocationData>) {
        polyline?.remove()
        listOfCircles.forEach { it.remove() }
        listOfCircles.clear()
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

            locations.forEach {
                var circleOptions = CircleOptions()
                circleOptions.radius(1.0)
                circleOptions.strokeColor(Color.TRANSPARENT)

                var accuracyCircleOptions = CircleOptions()

                accuracyCircleOptions.strokePattern(listOf(Dot()))
                accuracyCircleOptions.strokeWidth(5.0f)

                when (it.provider.toUpperCase()) {
                    "GPS" -> {
                        circleOptions.fillColor(Color.RED)
                        accuracyCircleOptions.strokeColor(Color.RED)
                    }
                    "NETWORK" -> {
                        circleOptions.fillColor(Color.GREEN)
                        accuracyCircleOptions.strokeColor(Color.GREEN)
                    }
                }
                circleOptions.center(LatLng(it.latitude, it.longitude))
                listOfCircles.add(map.addCircle(circleOptions))

                if (showAccuracy) {
                    accuracyCircleOptions.center(LatLng(it.latitude, it.longitude))
                    accuracyCircleOptions.radius(it.accuracy.toDouble())
                    listOfCircles.add(map.addCircle(accuracyCircleOptions))

                }
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
        }
    }

    private fun onStartNextButtonClick() {
        if (routeStarted) {
            try {
                val position = model.data.route.value.first { it.tsArrival == 0L || it.tsDepature == 0L }
                if (position.tsArrival == 0L) {
                    position.tsArrival = System.currentTimeMillis()
                } else {
                    position.tsDepature = System.currentTimeMillis()
                }

                model.data.route.notifyObserver()
            } catch (e: Exception) {}
        } else {
            routeStarted = true
            btnRouteStartNext.text = getString(R.string.next)
            btnRouteStartNext.setBackgroundColor(requireActivity().getColor(R.color.design_default_color_secondary))
            model.data.route.notifyObserver()
        }

    }

    private fun onResetButtonClick() {
        routeStarted = false
        model.data.route.value.forEach { it.tsArrival = 0L; it.tsDepature = 0L; }
        model.data.route.notifyObserver()
        btnRouteStartNext.text = getString(R.string.start)
        btnRouteStartNext.setBackgroundColor(requireActivity().getColor(R.color.design_default_color_primary))
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

        showAccuracy = PreferenceHelper.getBoolean(
                context,
                preferences,
                R.string.setting_google_maps_show_accuracy
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.setting_google_maps_zoom_factor),
            getString(R.string.setting_google_maps_follow_timeout),
            getString(R.string.setting_google_maps_enable_polyline),
            getString(R.string.setting_google_maps_show_accuracy) -> {
                loadPreferences()
            }
            getString(R.string.setting_google_maps_follow_location) -> {
                loadPreferences()
                follow = keepFollowing
            }
        }
    }
    companion object {
        // Convert a view to bitmap
        fun createDrawableFromView(context: Context, view: View): Bitmap? {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }
    }
}