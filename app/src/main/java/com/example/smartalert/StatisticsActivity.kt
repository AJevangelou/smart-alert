package com.example.smartalert

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.smartalert.databinding.ActivityAdminBinding
import com.example.smartalert.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatisticsActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var legend: Legend
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val toolbar: Toolbar = findViewById(R.id.toolbarStat)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        barChart = findViewById(R.id.idBarChart)
        legend = barChart.legend
        legend.isEnabled = true
        legend.textSize = 16f
        legend.formSize = 12f
        legend.formToTextSpace = 8f
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.orientation = Legend.LegendOrientation.HORIZONTAL

        val entries = listOf("Fire", "Flood", "Earthquake", "Other")
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)

        val legendEntries = ArrayList<LegendEntry>()
        for (i in entries.indices) {
            val entry = LegendEntry()
            entry.label = entries[i]
            entry.formColor = colors[i]
            legendEntries.add(entry)
        }

        legend.setCustom(legendEntries)

        databaseReference = FirebaseDatabase.getInstance().reference.child("statistics")

        val barEntriesList = ArrayList<BarEntry>()

        val childValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var fireCount = 0
                var floodCount = 0
                var earthquakeCount = 0
                var otherCount = 0

                for (childSnapshot in dataSnapshot.children) {
                    val type: String? = childSnapshot.child("type").getValue(String::class.java)

                    if (type == "fire") {
                        fireCount += 1
                    } else if (type == "flood") {
                        floodCount += 1
                    } else if (type == "earthquake") {
                        earthquakeCount += 1
                    } else {
                        otherCount += 1
                    }
                }

                barEntriesList.add(BarEntry(0f, fireCount.toFloat()))
                barEntriesList.add(BarEntry(1f, floodCount.toFloat()))
                barEntriesList.add(BarEntry(2f, earthquakeCount.toFloat()))
                barEntriesList.add(BarEntry(3f, otherCount.toFloat()))

                val barDataSet = BarDataSet(barEntriesList, "Disaster Types")
                barDataSet.setColors(colors)

                val barData = BarData(barDataSet)
                barData.barWidth = 0.6f
                // Set X and Y label colors to white
                if(isDarkModeEnabled(applicationContext)) {
                    barChart.xAxis.textColor = Color.WHITE
                    barChart.axisLeft.textColor = Color.WHITE
                    barChart.axisRight.textColor = Color.WHITE
                    legend.textColor = Color.WHITE
                }else{
                    barChart.xAxis.textColor = Color.BLACK
                    barChart.axisLeft.textColor = Color.BLACK
                    barChart.axisRight.textColor = Color.BLACK
                    legend.textColor = Color.BLACK
                }


                barChart.data = barData
                barChart.setFitBars(true)
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(entries)
                barChart.xAxis.granularity = 1f
                barChart.description.isEnabled = false
                barChart.invalidate()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ChildValueListener", "Error: ${databaseError.message}")
            }
        }

        databaseReference.addListenerForSingleValueEvent(childValueEventListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun getColorFromAttribute(attrId: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }
    fun isDarkModeEnabled(context: Context): Boolean {
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

}