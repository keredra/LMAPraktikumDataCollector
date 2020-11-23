package de.krd.lmapraktikum_datacollector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import de.krd.lmapraktikum_datacollector.permission.PermissionActivity
import de.krd.lmapraktikum_datacollector.recorder.LocationRecorder
import de.krd.lmapraktikum_datacollector.recorder.SensorRecorder
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.*
import kotlin.math.log


class MainActivity : PermissionActivity() {
    private val model: GlobalModel by viewModels()
    private lateinit var locationRecorder: LocationRecorder
    private lateinit var sensorRecorder: SensorRecorder
    private lateinit var appBarConfiguration: AppBarConfiguration

    companion object {
        private val OPEN_REQUEST_CODE = 41
        private val SAVE_REQUEST_CODE = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationRecorder = LocationRecorder(this, model)
        sensorRecorder = SensorRecorder(this, model)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        button_recording.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                locationRecorder.start()
                sensorRecorder.start()
            } else {
                locationRecorder.stop()
                sensorRecorder.stop()
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_location_data,
                R.id.nav_sensor_data
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val navController = findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.settingsFragment)
            }
            R.id.action_load_data -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/json"
                startActivityForResult(intent, OPEN_REQUEST_CODE)
            }
            R.id.action_save_data -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/json"
                intent.putExtra(Intent.EXTRA_TITLE, "newfile.json")

                startActivityForResult(intent, SAVE_REQUEST_CODE)
            }
            R.id.action_delete_data -> {
                model.data.locations.clear()
                model.data.sensorEvents.clear()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        resultData: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultData)
        var currentUri: Uri? = null
        if (resultCode == RESULT_OK) {
            if (requestCode == SAVE_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.data!!
                    writeFileContent(currentUri, "ABCDE")
                }
            } else if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.data!!
                    try {
                        val content: String? = readFileContent(currentUri)
                        Log.i("FileReader", ""+content)
                    } catch (e: IOException) {
                        // Handle error here
                    }
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun readFileContent(uri: Uri): String? {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val reader = BufferedReader(
            InputStreamReader(
                inputStream
            )
        )
        val stringBuilder = StringBuilder()
        reader.forEachLine {
            stringBuilder.append(it)
        }
        inputStream?.close()
        return stringBuilder.toString()
    }

    private fun writeFileContent(uri: Uri, content: String) {
        try {
            val pfd = this.contentResolver.openFileDescriptor(uri, "w")
            val fileOutputStream = FileOutputStream(
                pfd!!.fileDescriptor
            )
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
            pfd!!.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}