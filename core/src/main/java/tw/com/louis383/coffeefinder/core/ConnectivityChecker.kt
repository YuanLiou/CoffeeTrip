package tw.com.louis383.coffeefinder.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

class ConnectivityChecker(context: Context) {

    private val connectivityManager by lazy {
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isCellularAvailable = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        val isWiFiAvailable = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        // There is a bug which is network capability will return false if a device connected to VPN.
        // It's often happened in Samsung devices such as Galaxy S8/Note8
        val isVpnEnabled = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        val isNormalNetworkAvailable = (isCellularAvailable || isWiFiAvailable)
        val isVpnNetworkAvailable = (isVpnEnabled && isWiFiEnabledExactly())
        return isNormalNetworkAvailable || isVpnNetworkAvailable
    }

    private fun isWiFiEnabledExactly(): Boolean {
        return wifiManager.isWifiEnabled
    }
}