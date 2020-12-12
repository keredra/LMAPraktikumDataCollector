package de.krd.lmapraktikum_datacollector.ui.cdf

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.map
import com.google.android.gms.maps.model.LatLng
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.krd.lmapraktikum_datacollector.GlobalModel
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import de.krd.lmapraktikum_datacollector.data.PositionEvaluationData
import kotlinx.android.synthetic.main.fragment_cdf.*


/**
 * A simple [Fragment] subclass.
 * Use the [CdfFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CdfFragment : Fragment() {
    private val model: GlobalModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cdf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        graph.getViewport().setXAxisBoundsManual(true)
        graph.getViewport().setYAxisBoundsManual(true)
        graph.getViewport().setMinY(0.0)
        graph.getViewport().setMaxY(1.0)
        if (model.data.route.value.size >= 2) {
            val errorValues = mutableListOf<Double>()

            val routeIterator = model.data.route.value.iterator()
            var routeStartPoint = routeIterator.next()
            while (routeIterator.hasNext()) {
                val routeEndPoint = routeIterator.next()

                val routeLocationData = getLocationDataInRange(routeStartPoint.tsDepature, routeEndPoint.tsArrival)
                for (i in routeLocationData.indices) {
                    val elementNum = i + 1
                    errorValues.add(getMaxError(routeLocationData.subList(0, elementNum)))
                }
                routeStartPoint = routeEndPoint
            }


            val sortedErrorValues = errorValues.sortedBy { value -> value }

            val dataPoints = mutableListOf<DataPoint>()
            for (i in sortedErrorValues.indices) {
                val elementNum = i + 1
                dataPoints.add(DataPoint(sortedErrorValues.get(i),
                    elementNum.toDouble() / sortedErrorValues.size))
            }

            graph.getViewport().setMinX(0.0)
            graph.getViewport().setMaxX(sortedErrorValues.last())
            val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints.toTypedArray())
            graph.addSeries(series)

        }
    }

    private fun getLocationDataInRange(start: Long, end: Long) : List<LocationData> {
        return model.data.locations.value.filter {
                locationData -> locationData.timestamp >= start && locationData.timestamp <=end
        }
    }

    private fun getMaxError(locations : List<LocationData>) : Double {
        var maxDistanceError = 0.0

        locations.forEach {l ->
            val routeSectionStart = model.data.route.value.find { pe -> pe.tsDepature <= l.timestamp }!!
            val routeSectionEnd = model.data.route.value.find { pe -> pe.tsArrival > l.timestamp }!!

            val realLatLng = getPositionByTimestamp(routeSectionStart, routeSectionEnd, l.timestamp)
            val realLocation = Location("")
            realLocation.latitude = realLatLng.latitude
            realLocation.longitude = realLatLng.longitude

            val recordedLocation = Location("")
            recordedLocation.latitude = l.latitude
            recordedLocation.longitude = l.longitude

            val distanceError = realLocation.distanceTo(recordedLocation).toDouble()
            if (distanceError > maxDistanceError) {
                maxDistanceError = distanceError
            }
        }

        return maxDistanceError
    }

    private fun getPositionByTimestamp(
        routeSectionStart: PositionEvaluationData,
        routeSectionEnd: PositionEvaluationData,
        timestamp: Long) : LatLng {
        val process = 1 - (routeSectionEnd.tsArrival - timestamp).toDouble() / (routeSectionEnd.tsArrival - routeSectionStart.tsDepature)

        val diff = LatLng(routeSectionEnd.latLng.latitude - routeSectionStart.latLng.latitude,
                          routeSectionEnd.latLng.longitude - routeSectionStart.latLng.longitude)

        return LatLng(routeSectionStart.latLng.latitude + diff.latitude * process,
                      routeSectionStart.latLng.longitude + diff.longitude * process)
    }
}