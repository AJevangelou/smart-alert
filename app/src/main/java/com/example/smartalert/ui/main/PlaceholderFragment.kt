package com.example.smartalert.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalert.FirebaseData
import com.example.smartalert.FiredataAdapter
import com.example.smartalert.databinding.FragmentAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.smartalert.R.drawable


/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentAdminBinding? = null
    private val fireDataList: MutableList<FirebaseData> = mutableListOf()
    private val fireDrawable = drawable.fire_button
    private val floodDrawable = drawable.flood_button
    private val earthquakeDrawable = drawable.earthquake_button
    private val otherDrawable = drawable.other_button

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        val root = binding.root
        fireDataList.clear()

        val recyclerView: RecyclerView = binding.recyclerView
        val sectionNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
        // Inside PlaceholderFragment's onCreateView method

        // Create a LinearLayoutManager
        val layoutManager = LinearLayoutManager(requireContext())

        // Set the layout manager for the RecyclerView
        recyclerView.layoutManager = layoutManager
        // Create an instance of your RecyclerViewAdapter with the retrieved item list
        val firebaseAdapter = FiredataAdapter(fireDataList)

        // Set the adapter for the RecyclerView
        recyclerView.adapter = firebaseAdapter

        when (sectionNumber) {
            1 -> getData(firebaseAdapter, fireDataList, fireDrawable, "fire")
            2 -> getData(firebaseAdapter, fireDataList, earthquakeDrawable, "earthquake")
            3 -> getData(firebaseAdapter, fireDataList, floodDrawable, "flood")
            // Add more cases for additional tabs if needed
            else -> getData(firebaseAdapter, fireDataList, otherDrawable, "other")
        }
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun getData(recyclerViewAdapter: FiredataAdapter, fireDataList: MutableList<FirebaseData>, imageDrawable: Int, path: String) {
    val database = FirebaseDatabase.getInstance().reference
    fireDataList.clear() // Clear the list before populating
    database.child("pending").child(path).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {


            for (countrySnapshot in dataSnapshot.children) {
                Log.d("Country Snapshot", countrySnapshot.toString())
                for (citySnapshot in countrySnapshot.children) {
                    Log.d("City Snapshot", citySnapshot.toString())
                    for (fireSnapshot in citySnapshot.children) {
                        Log.d("Fire Snapshot", fireSnapshot.toString())

////                      Parse the values from the fireSnapshot
                        val count = fireSnapshot.child("count").getValue(Long::class.java)
                        val latitude = fireSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = fireSnapshot.child("longitude").getValue(Double::class.java)
                        val timestamp = fireSnapshot.child("timestamp").getValue(String::class.java)
                        val descriptionList: MutableList<String> = mutableListOf()
                        val photosList: MutableList<String> = mutableListOf()
                        val country = countrySnapshot.key
                        val city = citySnapshot.key
                        val key = fireSnapshot.key

                        for (childSnapshot in fireSnapshot.child("description").children) {
                            val description = childSnapshot.getValue(String::class.java)
                            description?.let {
                                descriptionList.add(it)
                            }
                        }
//                      // Create a FireData object and add it to the list
                        val fireData = FirebaseData(count, descriptionList, latitude, longitude, timestamp, imageDrawable, photosList, country, city, key, path)
                        fireDataList.add(fireData)
                        Log.d("Fire Data", fireData.toString())
                    }
                }
            }


            // Log the populated fireDataList
            for (fireData in fireDataList) {
                Log.d("Fire Data", fireData.toString())
            }

            // Notify the RecyclerView adapter that the data has changed
            recyclerViewAdapter.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase Error", error.message)
        }
    })
}

