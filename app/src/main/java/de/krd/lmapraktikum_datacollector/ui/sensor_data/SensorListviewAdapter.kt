package de.krd.lmapraktikum_datacollector.ui.sensor_data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.krd.lmapraktikum_datacollector.R
import de.krd.lmapraktikum_datacollector.data.SensorData
import java.util.*
import kotlin.collections.ArrayList

class SensorListviewAdapter(
        private val context: Context,
        private val dataSource: ArrayList<SensorData>
) : BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ViewHolder(convertView: View) {
        var mTime: TextView? = null
        var mType: TextView? = null
        //var mName: TextView? = null
        var mValueX: TextView? = null
        var mValueY: TextView? = null
        var mValueZ: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val holder : ViewHolder

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sensor_data_row, null)
            holder = ViewHolder(convertView)
            convertView.setTag(holder)

            holder.mTime = convertView!!.findViewById<View>(R.id.tv_loRowTime) as TextView
            holder.mType = convertView.findViewById<View>(R.id.tv_loRowType) as TextView
            //holder.mName = convertView.findViewById<View>(R.id.tv_loRowName) as TextView
            holder.mValueX = convertView.findViewById<View>(R.id.tv_loRowValueX) as TextView
            holder.mValueY = convertView.findViewById<View>(R.id.tv_loRowValueY) as TextView
            holder.mValueZ = convertView.findViewById<View>(R.id.tv_loRowValueZ) as TextView
            convertView.setTag(holder)
        } else {
            holder = convertView.getTag() as ViewHolder
        }

        val item: SensorData = dataSource.get(position)

        val date = Date(item.timestamp)
        holder.mTime?.setText(date.toString())
        holder.mType?.setText(item.type.toString())
        //holder.mName?.setText(item.name)
        holder.mValueX?.setText(item.values[0].toString())
        holder.mValueY?.setText(item.values[1].toString())
        holder.mValueZ?.setText(item.values[2].toString())
        return convertView
    }
}