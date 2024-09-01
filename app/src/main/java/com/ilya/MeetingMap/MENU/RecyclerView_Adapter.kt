import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ilya.MeetingMap.MENU.Server_API.deleteParticipantMarker

import com.ilya.MeetingMap.Mine_menu.Main_menu
import com.ilya.MeetingMap.R
import com.ilya.reaction.logik.PreferenceHelper.getUserKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerAdapter(
    private val markerList: MutableList<MarkerData>, // Изменяемый список
    private val onMarkerClickListener: Main_menu, // Интерфейс в конструкторе
    private val uid: String
) : RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    private val key = getUserKey(onMarkerClickListener).toString()

    inner class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val markerName: TextView = itemView.findViewById(R.id.markerName)
        val markerDescription: TextView = itemView.findViewById(R.id.markerDescription)
        val stree: TextView = itemView.findViewById(R.id.marker_street)
        val starData: TextView = itemView.findViewById(R.id.marker_start_Date)
        val endData: TextView = itemView.findViewById(R.id.marker_end_Date)
        val find_marker_button = itemView.findViewById<Button>(R.id.marker_button_find_tag)
        val delte_marker_button = itemView.findViewById<Button>(R.id.marker_button_delete_tag)

        fun bind(marker: MarkerData) {
            markerName.text = marker.name
            markerDescription.text = marker.whatHappens
            stree.text = ""
            starData.text = "${marker.startDate} Time:${marker.startTime}"
            endData.text = "${marker.endDate} Time:${marker.endTime}"
            find_marker_button.setOnClickListener {
                onMarkerClickListener.onFindLocation(marker.lat, marker.lon)
            }
            delte_marker_button.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    val success = deleteParticipantMarker(uid, key, marker.id)
                    if (success) {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            markerList.removeAt(position) // Удаляем элемент из списка
                            notifyItemRemoved(position) // Уведомляем адаптер о удалении
                        }
                    }
                }
            }
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

    // Класс для добавления отступов между элементами списка в RecyclerView
    class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            // Добавляем отступы, кроме последнего элемента
            if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
                outRect.bottom = space
            }
        }
    }


