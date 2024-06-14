package com.mastermovilesua.beaconsraul.ui

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mastermovilesua.beaconsraul.databinding.FragmentBeaconScanBinding
import org.altbeacon.beacon.*

class BeaconScanFragment : Fragment(), BeaconConsumer {

    private var _binding: FragmentBeaconScanBinding? = null
    private val binding get() = _binding!!

    private var beaconManager: BeaconManager? = null
    private var region: Region? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconManager = BeaconManager.getInstanceForApplication(requireActivity())
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager!!.bind(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeaconScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uuidEditText.setText("A3083E3D-BD69-4703-9DA3-3924C2AB07E2")
        binding.majorEditText.setText("1")
        binding.minorEditText.setText("2")

        binding.scanButton.setOnClickListener {
            startScanning()
        }
    }


    private fun startScanning() {
        val uuid = binding.uuidEditText.text.toString()
        val major = binding.majorEditText.text.toString().toInt()
        val minor = binding.minorEditText.text.toString().toInt()

        region = Region(
            "myRegion",
            Identifier.parse(uuid),
            Identifier.parse(major.toString()),
            Identifier.parse(minor.toString())
        )

        try {
            beaconManager!!.startRangingBeaconsInRegion(region!!)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }


    override fun onBeaconServiceConnect() {
        beaconManager!!.addRangeNotifier { beacons, region ->
            if (beacons.isNotEmpty()) {
                for (beacon in beacons) {
                    val proximity = beacon.distance
                    val rssi = beacon.rssi
                    val timestamp = System.currentTimeMillis()

                    // Actualizar la UI con los datos del beacon
                    activity?.runOnUiThread {
                        binding.beaconDataTextView.text = "Proximidad: $proximity\n" +
                                "RSSI: $rssi\n" +
                                "Timestamp: $timestamp"
                    }
                }
            }
        }
    }

    override fun getApplicationContext(): Context {
        return requireContext()
    }

    override fun unbindService(connection: ServiceConnection?) {
        beaconManager!!.unbind(this)
    }

    override fun bindService(intent: Intent?, connection: ServiceConnection?, mode: Int): Boolean {
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager!!.unbind(this)
    }
}
