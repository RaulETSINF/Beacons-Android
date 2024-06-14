package com.mastermovilesua.beaconsraul

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mastermovilesua.beaconsraul.ui.BeaconScanFragment
import com.mastermovilesua.beaconsraul.ui.BeaconTransmitFragment
import org.altbeacon.beacon.service.BeaconService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_FINE_LOCATION
                )
            } else {
                startBeaconService()
            }
        } else {
            startBeaconService()
        }
    }

    private fun startBeaconService() {
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(BeaconScanFragment(), "Escanear")
        adapter.addFragment(BeaconTransmitFragment(), "Emitir")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        startService(Intent(this, BeaconService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BeaconService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBeaconService()
            } else {

            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }
}

internal class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val fragmentTitleList: MutableList<String> = ArrayList()
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }
}
