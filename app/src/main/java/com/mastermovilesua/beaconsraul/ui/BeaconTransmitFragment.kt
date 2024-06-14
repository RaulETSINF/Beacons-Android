package com.mastermovilesua.beaconsraul.ui


import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter
import java.util.Arrays
import android.widget.Toast
import com.mastermovilesua.beaconsraul.R
import com.mastermovilesua.beaconsraul.databinding.FragmentBeaconTransmitBinding

class BeaconTransmitFragment : Fragment() {

    private var beaconTransmitter: BeaconTransmitter? = null

    lateinit var binding: FragmentBeaconTransmitBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBeaconTransmitBinding.inflate(inflater,container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uuidEditText.setText("A3083E3D-BD69-4703-9DA3-3924C2AB07E2")
        binding.majorEditText.setText("1")
        binding.minorEditText.setText("2")

        binding.transmitButton.setOnClickListener {
            startTransmitting()
        }
    }

    private fun startTransmitting() {
        val uuid = binding.uuidEditText.text.toString()
        val major = binding.majorEditText.text.toString().toInt()
        val minor = binding.minorEditText.text.toString().toInt()

        val beacon = Beacon.Builder()
            .setId1(uuid)
            .setId2(major.toString())
            .setId3(minor.toString())
            .setManufacturer(0x0118)
            .setTxPower(-59)
            .setDataFields(listOf(*arrayOf(0L)))
            .build()
        val beaconParser = BeaconParser()
            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        beaconTransmitter = BeaconTransmitter(activity, beaconParser)
        beaconTransmitter!!.startAdvertising(beacon, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Toast.makeText(activity, "Transmisión de Beacon iniciada", Toast.LENGTH_SHORT).show()
            }

            override fun onStartFailure(errorCode: Int) {
                Toast.makeText(activity, "Fallo en la transmisión de Beacon: $errorCode", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
