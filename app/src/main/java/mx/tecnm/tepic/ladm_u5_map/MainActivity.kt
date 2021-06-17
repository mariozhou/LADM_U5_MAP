package mx.tecnm.tepic.ladm_u5_map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    var siPermiso = 1
    var datalista = ArrayList<String>()
    var listaId = ArrayList<String>()
    lateinit var locacion: LocationManager
    var pos1: Location = Location("")
    var pos2: Location = Location("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn)
        val lista = findViewById<TextView>(R.id.lista)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                siPermiso
            )
        } else {
            locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var oyente = Oyente(this)
            locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 00f, oyente)
        }
        baseRemota.collection("tec")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    mensaje(error.message!!)
                    return@addSnapshotListener
                }
                var resultado = ""
                posicion.clear()
                datalista.clear()
                listaId.clear()
                for (document in querySnapshot!!) {
                        var data = Data()
                        data.nombre = document.getString("nombre").toString()
                        data.posicion1 = document.getGeoPoint("posicion1")!!
                        data.posicion2 = document.getGeoPoint("posicion2")!!
                        data.Dentro = document.getString("dentro")!!
                        resultado += data.toString() + "\n"
                        posicion.add(data)

                }
                lista.setText(resultado)
            }

        btn.setOnClickListener {
            val edbusqueda = findViewById<EditText>(R.id.edbusqueda)
            val textresultado = findViewById<TextView>(R.id.textresultado)
            baseRemota.collection("tec")
                    .whereEqualTo("nombre", edbusqueda.getText().toString())
                    .addSnapshotListener { querySnapshot, error ->
                        if (error != null) {
                            mensaje(error.message!!)
                            return@addSnapshotListener
                        }
                        for (document in querySnapshot!!) {
                            pos1.longitude = document.getGeoPoint("posicion1")!!.longitude
                            pos1.latitude = document.getGeoPoint("posicion1")!!.latitude
                            pos2.longitude = document.getGeoPoint("posicion2")!!.longitude
                            pos2.latitude = document.getGeoPoint("posicion2")!!.latitude
                        }
                        var coor = "(${(pos1.latitude)}, ${pos1.longitude})\n(${pos2.latitude}, ${pos2.longitude})"
                        textresultado.setText(coor)
                    }
        }
    }

    private fun mensaje(m: String) {
        AlertDialog.Builder(this)
            .setTitle("Atencion")
            .setMessage(m)
            .setPositiveButton("OK") { d, i -> }
            .show()
    }

    private fun alerta(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}


class Oyente(puntero:MainActivity) : LocationListener {
    var p = puntero
    val coo = p.findViewById<TextView>(R.id.coo)
    val ubtext = p.findViewById<TextView>(R.id.ubtext)
    override fun onLocationChanged(location: Location) {
        coo.setText("Coordenadas:\n${location.latitude}, ${location.longitude}")
       // coo.setText("")
        var posicionActual = GeoPoint(location.latitude, location.longitude)
        for (item in p.posicion) {
            if (item.estoyEn(posicionActual)) {
                ubtext.setText("Esta en:\n${item.nombre}")
            }
        }
    }
}