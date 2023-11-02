package com.example.lab11_ppm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.example.lab11_ppm.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity() {
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    val PERMISSION_ID = 42



    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(allPermissionsGrantedGPS()){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            leerUbicacionActual()
        } else{
            ActivityCompat.requestPermissions(this,arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ID)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }


        binding.btnDetectar.setOnClickListener{
            leerUbicacionActual()
        }
    }

    private fun allPermissionsGrantedGPS() = Companion.REQUIRED_PERMISSIONS_GPS.all{
        ContextCompat.checkSelfPermission(baseContext,it)== PackageManager.PERMISSION_GRANTED
    }

    private fun leerUbicacionActual(){
        if (checkPermissions()){
            if(isLocationEnabled()){
                if(ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                        var location: Location? = task.result
                        if(location==null){
                            requestNewLocationData()
                        } else{
                            binding.tv1.text = "LATITUD = "+location.latitude.toString()
                            binding.tv2.text = "LONGITUD = "+location.longitude.toString()
                        }
                    }

                }
            } else{
                Toast.makeText(this,"Activar Ubicacion",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                this.finish()
            }
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),PERMISSION_ID)
        }
    }

    private fun requestNewLocationData(){
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority= com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallBack, Looper.myLooper())
        }

    }

    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            var mLastLocation : Location? = locationResult?.lastLocation ?: null
            binding.tv1.text = "LATITUD = "+mLastLocation?.latitude.toString()
            binding.tv2.text = "LONGITUD = "+mLastLocation?.longitude.toString()
        }
    }

    private fun isLocationEnabled():Boolean{
        var locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions():Boolean{
    if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        return true
    } else{
        return false
    }
    }

    companion object{
        private val REQUIRED_PERMISSIONS_GPS= arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}