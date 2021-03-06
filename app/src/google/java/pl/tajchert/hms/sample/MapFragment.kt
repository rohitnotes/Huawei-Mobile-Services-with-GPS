package pl.tajchert.hms.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.google.map_fragment.*
import pl.tajchert.hms.sample.common.MapMarkerFactory
import pl.tajchert.hms.sample.data.Station
import pl.tajchert.hms.sample.databinding.MapFragmentBinding


class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() = MapFragment()
    }

    private val viewModel: MapViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private val mapMarkerFactory = MapMarkerFactory()

    private val changeObserver = Observer<List<Station>> { stationList ->
        stationList?.let {
            println("StationCount ${stationList.count()}")
            googleMap?.clear()
            stationList.forEach { station ->
                context?.let { context ->
                    googleMap?.addMarker(mapMarkerFactory.getMarkerForStation(station, context))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MapFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.fragment = this
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        this.activity?.let { MapsInitializer.initialize(it) }
        return binding.root
    }

    fun onClickRandomized(v: View) {
        viewModel.getRandomStations()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mapView.onSaveInstanceState(savedInstanceState)
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onMapReady(gMap: GoogleMap) {
        println("Map ready")
        googleMap = gMap
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        //Restore last know map position
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    viewModel.lastMapLocationLat,
                    viewModel.lastMapLocationLng
                ), viewModel.lastMapLocationZoom
            )
        )
        //Save current position to recreate map in same place
        googleMap?.setOnCameraIdleListener {
            googleMap?.cameraPosition?.let {
                viewModel.lastMapLocationLat = it.target.latitude
                viewModel.lastMapLocationLng = it.target.longitude
                viewModel.lastMapLocationZoom = it.zoom
            }
        }
        viewModel.liveStations.observe(viewLifecycleOwner, changeObserver)
    }
}