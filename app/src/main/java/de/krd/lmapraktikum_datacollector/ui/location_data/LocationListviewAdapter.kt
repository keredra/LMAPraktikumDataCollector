package de.krd.lmapraktikum_datacollector.ui.location_data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.LocationData
import java.util.*


class LocationListviewAdapter(
        private val context: Context,
        private val dataSource: ArrayList<LocationData>
) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): LocationData {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ViewHolder(convertView: View) {
        var mTime: TextView? = null
        var mProvider: TextView? = null
        var mLatitude: TextView? = null
        var mLongitude: TextView? = null
        var mAltitude: TextView? = null
        var mAccuracy: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val holder : ViewHolder

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.location_data_row, null)
            holder = ViewHolder(convertView)
            convertView.setTag(holder)

            holder.mTime = convertView!!.findViewById<View>(R.id.tv_loRowTime) as TextView
            holder.mProvider = convertView.findViewById<View>(R.id.tv_loRowProv) as TextView
            holder.mLatitude = convertView.findViewById<View>(R.id.tv_loRowLat) as TextView
            holder.mLongitude = convertView.findViewById<View>(R.id.tv_loRowLongit) as TextView
            holder.mAltitude = convertView.findViewById<View>(R.id.tv_loRowAlt) as TextView
            holder.mAccuracy = convertView.findViewById<View>(R.id.tv_loRowAcc) as TextView


            convertView.setTag(holder)
        } else {
            holder = convertView.getTag() as ViewHolder
        }

        val item: LocationData = dataSource.get(position)
        val date = Date(item.timestamp)
        holder.mTime?.setText(date.toString())
        holder.mProvider?.setText(item.provider)
        holder.mLatitude?.setText(item.latitude.toString())
        holder.mLongitude?.setText(item.longitude.toString())
        holder.mAltitude?.setText(item.altitude.toString())
        holder.mAccuracy?.setText(item.accuracy.toString())

        return convertView
    }
}