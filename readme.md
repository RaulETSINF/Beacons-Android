# Proyecto de Beacons
Raúl Piqueras Melero

## Descripción general del proyecto
Este proyecto es una aplicación de Android diseñada para trabajar con beacons. La aplicación tiene dos modos de funcionamiento principales: escanear beacons y emitir señales de beacon. La interfaz de usuario se divide en dos pestañas para manejar estas funcionalidades de manera independiente.

## Funcionalidades de la aplicación

### Escaneo de Beacons
En la pestaña de escaneo, la aplicación permite al usuario especificar los datos del beacon que desea escanear, incluidos UUID, major y minor number. La aplicación proporciona valores por defecto para estos campos para facilitar las pruebas. Cuando el usuario inicia el escaneo, la aplicación comienza a buscar la región indicada y, una vez encontrada, muestra el identificador de la región y comienza a mostrar datos de ranging del beacon, incluyendo proximidad, intensidad de la señal (RSSI), precisión de la proximidad y un timestamp.


#### Código del Fragmento de Escaneo

```kotlin
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

```


### Emisión de Beacons
En la pestaña de emisión, la aplicación permite al usuario simular un beacon introduciendo un UUID, major y minor number. De nuevo, se proporcionan valores por defecto para facilitar las pruebas. Al iniciar la transmisión, la aplicación comienza a emitir una señal de beacon con los datos especificados y notifica al usuario si la transmisión fue exitosa o fallida.

```kotlin
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
```

### Manifest
El manifiesto declara los permisos necesarios para el funcionamiento de los beacons y el servicio de beacon.

```xml
<service android:name="org.altbeacon.beacon.service.BeaconService"/>

<uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
```


### Archivo Gradle
La configuración de Gradle incluye la dependencia de la biblioteca de beacons.

```gradle
implementation("org.altbeacon:android-beacon-library:2.19.3")
```

