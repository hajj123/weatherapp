package com.example.weatherapp.fragments.location
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import org.koin.androidx.viewmodel.ext.android.viewModel

import androidx.recyclerview.widget.DividerItemDecoration

import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.data.RemoteLocation
import com.example.weatherapp.fragments.home.HomeFragment
import com.example.weatherapp.databinding.FragmentLocationBinding


class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val locationViewModel: LocationViewModel by viewModel()

    private val locationsAdapter = LocationsAdapter(
        onLocationClicked = { remoteLocation ->
            setLocation(remoteLocation)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Change to nullable return type
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setupLocationsRecyclerView()
        set0bservers()
    }
    private fun setupLocationsRecyclerView(){
        with(binding.locationsRecyclerView){
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            adapter = locationsAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to avoid memory leaks
    }

    private fun setListeners() {
        binding.imageClose.setOnClickListener { findNavController().popBackStack() }
        binding.inputSearch.editText?.setOnEditorActionListener {_, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                hideSoftkeyboard()
                val query = binding.inputSearch.editText?.text
                if (query.isNullOrBlank()) return@setOnEditorActionListener true
                searchLocation(query.toString())
            }
            return@setOnEditorActionListener true
        }

    }



    private fun setLocation(remoteLocation: RemoteLocation) {
        with (remoteLocation) {
            val locationText = "$name, $region, $country"
            setFragmentResult(
                requestKey = HomeFragment.HomeFragment.REQUEST_KEY_MANUAL_LOCATION_SEARCH,
                result = bundleOf(
                    HomeFragment.HomeFragment.KEY_LOCATION_TEXT to locationText,
                    HomeFragment.HomeFragment.KEY_LATITUDE to lat,
                    HomeFragment.HomeFragment.KEY_LONGITUDE to lon
                )
            )
            findNavController().popBackStack()
        }
    }


    private fun set0bservers () {
        locationViewModel.searchResult.observe (viewLifecycleOwner ) {
            val searchResultDataState = it ?: return@observe
            if (searchResultDataState.isLoading) {
                binding.locationsRecyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
            searchResultDataState. locations?. let { remoteLocations ->
                binding.locationsRecyclerView.visibility = View.VISIBLE
                locationsAdapter.setData(remoteLocations)
            }
            searchResultDataState.error?.let{error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchLocation(query: String){
        locationViewModel.searchLocation(query)
    }
    private fun hideSoftkeyboard(){
        val inputManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            binding.inputSearch.editText?.windowToken,0
        )
    }
}