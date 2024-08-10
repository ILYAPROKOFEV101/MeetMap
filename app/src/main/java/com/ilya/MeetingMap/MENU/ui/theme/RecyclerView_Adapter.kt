import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilya.MeetingMap.R

class MarkerAdapter(private val markerList: List<MarkerData>) : RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    inner class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val markerName: TextView = itemView.findViewById(R.id.markerName)
        val markerDescription: TextView = itemView.findViewById(R.id.markerDescription)

        fun bind(marker: MarkerData) {
            markerName.text = marker.name
            markerDescription.text = marker.whatHappens
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marker, parent, false)
        return MarkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val marker = markerList[position]
        Log.d("MarkerAdapter", "Binding marker: $marker")
        holder.bind(marker)
    }


    override fun getItemCount(): Int {
        return markerList.size
    }
}
