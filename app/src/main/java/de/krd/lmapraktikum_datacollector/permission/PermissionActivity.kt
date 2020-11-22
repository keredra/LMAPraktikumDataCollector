package de.krd.lmapraktikum_datacollector.permission

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/*
 * This class helps to request access rights on runtime without having the need to make use of the onRequestPermissionsResult
 */
open class PermissionActivity : AppCompatActivity() {
    /*
     * All current open requests
     */
    private var requests = mutableListOf<PermissionRequest>()


    /*
     * Checks if permission is allready granted
     */
    private fun checkPermission(type: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, type) != PackageManager.PERMISSION_DENIED
    }

    /*
     * Requests a permission
     */
    private fun requestPermission(type: String) {
        ActivityCompat.requestPermissions(this, arrayOf(type), 0)
    }

    /*
     * First checks if a permission is already granted. If yes it executes the given callback.
     * If not it requests the needed permission and adds a new request to the open requests list.
     */
    public fun withPermission(type: String, func: () -> Unit) {
        if (checkPermission(type)) {
            func();
        } else {
            requests.add(PermissionRequest(type, func))
            requestPermission(type);
        }
    }

    /*
     * Here is where the magic happens :D
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!grantResults.contains(-1)) {
            val itr = requests.iterator()
            while (itr.hasNext()) {
                val request = itr.next()
                if (permissions.contains(request.type)) {
                    request.func()
                    itr.remove()
                }
            }
        }
    }
}