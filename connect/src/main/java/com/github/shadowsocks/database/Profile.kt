/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package com.github.shadowsocks.database

import android.annotation.TargetApi
import android.net.Uri
import android.os.Parcelable
import android.util.Base64
import android.util.LongSparseArray
import androidx.annotation.Keep
import androidx.core.net.toUri
import androidx.room.*
import com.github.shadowsocks.plugin.PluginConfiguration
import com.github.shadowsocks.plugin.PluginOptions
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.parsePort
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException
import java.util.*

@Keep
@Entity
@Parcelize
data class Profile(
    @PrimaryKey(autoGenerate = true)
        var id: Long = 0,

        // user configurable fields
    var name: String? = "",//notification show name
    var nodeParentName: String = "",//country
    var nodeChildName: String = "",//city
    var nodeParentCode: String = "",//code
    var nodeInFast: String = "",//if false from server list else from smart list

    var pa0: String = "",
    var pa1: String = "",
    var pa2: String = "",
    var pa3: String = "",
    var pa4: String = "",
    var pa5: String = "",
    var pa6: String = "",
    var pa7: String = "",
    var pa8: String = "",
    var pa9: String = "",
    var pa10: String = "",

    var host: String = "example.shadowsocks.org",
    var remotePort: Int = 8388,
    var password: String = "u1rRWTssNv0p",
    var method: String = "aes-256-cfb",

    var route: String = "all",
    var remoteDns: String = "dns.google",
    var proxyApps: Boolean = false,
    var bypass: Boolean = false,
    var udpdns: Boolean = false,
    var ipv6: Boolean = false,
    @TargetApi(28)
        var metered: Boolean = false,
    var individual: String = "",
    var plugin: String? = null,
    var udpFallback: Long? = null,

        // managed fields
    var subscription: SubscriptionStatus = SubscriptionStatus.UserConfigured,
    var tx: Long = 0,
    var rx: Long = 0,
    var userOrder: Long = 0,

    @Ignore // not persisted in db, only used by direct boot
        var dirty: Boolean = false
) : Parcelable, Serializable {
    enum class SubscriptionStatus(val persistedValue: Int) {
        UserConfigured(0),
        Active(1),
        /**
         * This profile is no longer present in subscriptions.
         */
        Obsolete(2),
        ;

        companion object {
            @JvmStatic
            @TypeConverter
            fun of(value: Int) = values().single { it.persistedValue == value }
            @JvmStatic
            @TypeConverter
            fun toInt(status: SubscriptionStatus) = status.persistedValue
        }
    }

    companion object {
        private const val serialVersionUID = 1L
        private val pattern =
                """(?i)ss://[-a-zA-Z0-9+&@#/%?=.~*'()|!:,;_\[\]]*[-a-zA-Z0-9+&@#/%=.~*'()|\[\]]""".toRegex()
        private val userInfoPattern = "^(.+?):(.*)$".toRegex()
        private val legacyPattern = "^(.+?):(.*)@(.+?):(\\d+?)$".toRegex()

        fun findAllUrls(data: CharSequence?, feature: Profile? = null) = pattern.findAll(data ?: "").map {
            val uri = it.value.toUri()
            try {
                if (uri.userInfo == null) {
                    val match = legacyPattern.matchEntire(String(Base64.decode(uri.host, Base64.NO_PADDING)))
                    if (match != null) {
                        val profile = Profile()
                        feature?.copyFeatureSettingsTo(profile)
                        profile.method = match.groupValues[1].lowercase(Locale.ENGLISH)
                        profile.password = match.groupValues[2]
                        profile.host = match.groupValues[3]
                        profile.remotePort = match.groupValues[4].toInt()
                        profile.plugin = uri.getQueryParameter(Key.plugin)
                        profile.name = uri.fragment
                        profile
                    } else {
                        Timber.e("Unrecognized URI: ${it.value}")
                        null
                    }
                } else {
                    val match = userInfoPattern.matchEntire(String(Base64.decode(uri.userInfo,
                            Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)))
                    if (match != null) {
                        val profile = Profile()
                        feature?.copyFeatureSettingsTo(profile)
                        profile.method = match.groupValues[1]
                        profile.password = match.groupValues[2]
                        // bug in Android: https://code.google.com/p/android/issues/detail?id=192855
                        try {
                            val javaURI = URI(it.value)
                            profile.host = javaURI.host ?: ""
                            if (profile.host.firstOrNull() == '[' && profile.host.lastOrNull() == ']') {
                                profile.host = profile.host.substring(1, profile.host.length - 1)
                            }
                            profile.remotePort = javaURI.port
                            profile.plugin = uri.getQueryParameter(Key.plugin)
                            profile.name = uri.fragment ?: ""
                            profile
                        } catch (e: URISyntaxException) {
                            Timber.e("Invalid URI: ${it.value}")
                            null
                        }
                    } else {
                        Timber.e("Unknown user info: ${it.value}")
                        null
                    }
                }
            } catch (e: IllegalArgumentException) {
                Timber.e("Invalid base64 detected: ${it.value}")
                null
            }
        }.filterNotNull()

        private class JsonParser(private val feature: Profile? = null) : ArrayList<Profile>() {
            val fallbackMap = mutableMapOf<Profile, Profile>()

            private val JsonElement?.optString get() = (this as? JsonPrimitive)?.asString
            private val JsonElement?.optBoolean
                get() = // asBoolean attempts to cast everything to boolean
                    (this as? JsonPrimitive)?.run { if (isBoolean) asBoolean else null }
            private val JsonElement?.optInt
                get() = try {
                    (this as? JsonPrimitive)?.asInt
                } catch (_: NumberFormatException) {
                    null
                }

            private fun tryParse(json: JsonObject, fallback: Boolean = false): Profile? {
                val host = json["server"].optString
                if (host.isNullOrEmpty()) return null
                val remotePort = json["server_port"]?.optInt
                if (remotePort == null || remotePort <= 0) return null
                val password = json["password"].optString
                if (password.isNullOrEmpty()) return null
                val method = json["method"].optString
                if (method.isNullOrEmpty()) return null
                return Profile().also {
                    it.host = host
                    it.remotePort = remotePort
                    it.password = password
                    it.method = method
                }.apply {
                    feature?.copyFeatureSettingsTo(this)
                    val id = json["plugin"].optString
                    if (!id.isNullOrEmpty()) {
                        plugin = PluginOptions(id, json["plugin_opts"].optString).toString(false)
                    }
                    name = json["remarks"].optString

                    nodeParentName = json["nodeParentName"].optString?:""
                    nodeChildName = json["nodeChildName"].optString?:""
                    nodeParentCode = json["nodeParentCode"].optString?:""

                    pa0 = json["pa0"].optString?:""
                    pa1 = json["pa1"].optString?:""
                    pa2 = json["pa2"].optString?:""
                    pa3 = json["pa3"].optString?:""
                    pa4 = json["pa4"].optString?:""
                    pa5 = json["pa5"].optString?:""
                    pa6 = json["pa6"].optString?:""
                    pa7 = json["pa7"].optString?:""
                    pa8 = json["pa8"].optString?:""
                    pa9 = json["pa9"].optString?:""
                    pa10 = json["pa10"].optString?:""

                    route = json["route"].optString ?: route
                    if (fallback) return@apply
                    remoteDns = json["remote_dns"].optString ?: remoteDns
                    ipv6 = json["ipv6"].optBoolean ?: ipv6
                    metered = json["metered"].optBoolean ?: metered
                    (json["proxy_apps"] as? JsonObject)?.also {
                        proxyApps = it["enabled"].optBoolean ?: proxyApps
                        bypass = it["bypass"].optBoolean ?: bypass
                        individual = (it["android_list"] as? JsonArray)?.asIterable()?.mapNotNull { it.optString }
                                ?.joinToString("\n") ?: individual
                    }
                    udpdns = json["udpdns"].optBoolean ?: udpdns
                    nodeInFast = json["nodeInFast"].optString ?: nodeInFast
                    (json["udp_fallback"] as? JsonObject)?.let { tryParse(it, true) }?.also { fallbackMap[this] = it }
                }
            }

            fun process(json: JsonElement?) {
                when (json) {
                    is JsonObject -> {
                        val profile = tryParse(json)
                        if (profile != null) add(profile) else for ((_, value) in json.entrySet()) process(value)
                    }
                    is JsonArray -> json.asIterable().forEach(this::process)
                    // ignore other types
                }
            }

            fun finalize(create: (Profile) -> Profile) {
                val profiles = ProfileManager.getAllProfiles() ?: emptyList()
                for ((profile, fallback) in fallbackMap) {
                    val match = profiles.firstOrNull {
                        fallback.host == it.host && fallback.remotePort == it.remotePort &&
                                fallback.password == it.password && fallback.method == it.method &&
                                it.plugin.isNullOrEmpty()
                    }
                    profile.udpFallback = (match ?: create(fallback)).id
                    ProfileManager.updateProfile(profile)
                }
            }
        }

        fun parseJson(json: JsonElement, feature: Profile? = null, create: (Profile) -> Profile) {
            JsonParser(feature).run {
                process(json)
                for (i in indices) {
                    val fallback = fallbackMap.remove(this[i])
                    this[i] = create(this[i])
                    fallback?.also { fallbackMap[this[i]] = it }
                }
                finalize(create)
            }
        }
    }

    @androidx.room.Dao
    interface Dao {
        @Query("SELECT * FROM `Profile` WHERE `id` = :id")
        operator fun get(id: Long): Profile?

        @Query("SELECT * FROM `Profile` WHERE `Subscription` != 2 ORDER BY `userOrder`")
        fun listActive(): List<Profile>

        @Query("SELECT * FROM `Profile`")
        fun listAll(): List<Profile>

        @Query("SELECT MAX(`userOrder`) + 1 FROM `Profile`")
        fun nextOrder(): Long?

        @Query("SELECT 1 FROM `Profile` LIMIT 1")
        fun isNotEmpty(): Boolean

        @Insert
        fun create(value: Profile): Long

        @Update
        fun update(value: Profile): Int

        @Query("DELETE FROM `Profile` WHERE `id` = :id")
        fun delete(id: Long): Int

        @Query("DELETE FROM `Profile`")
        fun deleteAll(): Int
    }

    val formattedAddress get() = (if (host.contains(":")) "[%s]:%d" else "%s:%d").format(host, remotePort)
    val formattedName get() = if (name.isNullOrEmpty()) formattedAddress else name!!

    fun copyFeatureSettingsTo(profile: Profile) {
        profile.route = route
        profile.ipv6 = ipv6
        profile.metered = metered
        profile.proxyApps = proxyApps
        profile.bypass = bypass
        profile.individual = individual
        profile.udpdns = udpdns

        profile.nodeParentName = nodeParentName
        profile.nodeParentCode = nodeParentCode
        profile.nodeChildName = nodeChildName
        profile.nodeInFast = nodeInFast

        profile.pa0 = pa0
        profile.pa1 = pa1
        profile.pa2 = pa2
        profile.pa3 = pa3
        profile.pa4 = pa4
        profile.pa5 = pa5
        profile.pa6 = pa6
        profile.pa7 = pa7
        profile.pa8 = pa8
        profile.pa9 = pa9
        profile.pa10 = pa10
    }

    fun toUri(): Uri {
        val auth = Base64.encodeToString("$method:$password".toByteArray(),
                Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
        val wrappedHost = if (host.contains(':')) "[$host]" else host
        val builder = Uri.Builder()
                .scheme("ss")
                .encodedAuthority("$auth@$wrappedHost:$remotePort")
        val configuration = PluginConfiguration(plugin ?: "")
        if (configuration.selected.isNotEmpty()) {
            builder.appendQueryParameter(Key.plugin, configuration.getOptions().toString(false))
        }
        if (!name.isNullOrEmpty()) builder.fragment(name)
        return builder.build()
    }

    override fun toString() = toUri().toString()

    fun toJson(profiles: LongSparseArray<Profile>? = null): JSONObject = JSONObject().apply {
        put("server", host)
        put("server_port", remotePort)
        put("password", password)

        put("nodeParentName", nodeParentName)
        put("nodeParentCode", nodeParentCode)
        put("nodeChildName", nodeChildName)
        put("method", method)
        if (profiles == null) return@apply
        PluginConfiguration(plugin ?: "").getOptions().also {
            if (it.id.isNotEmpty()) {
                put("plugin", it.id)
                put("plugin_opts", it.toString())
            }
        }
        put("remarks", name)
        put("nodeInFast", nodeInFast)
        put("pa0", pa0)
        put("pa1", pa1)
        put("pa2", pa2)
        put("pa3", pa3)
        put("pa4", pa4)
        put("pa5", pa5)
        put("pa6", pa6)
        put("pa7", pa7)
        put("pa8", pa8)
        put("pa9", pa9)
        put("pa10", pa10)
        put("route", route)
        put("remote_dns", remoteDns)
        put("ipv6", ipv6)
        put("metered", metered)
        put("proxy_apps", JSONObject().apply {
            put("enabled", proxyApps)
            if (proxyApps) {
                put("bypass", bypass)
                // android_ prefix is used because package names are Android specific
                put("android_list", JSONArray(individual.split("\n")))
            }
        })
        put("udpdns", udpdns)
        val fallback = profiles.get(udpFallback ?: return@apply)
        if (fallback != null && fallback.plugin.isNullOrEmpty()) fallback.toJson().also { put("udp_fallback", it) }
    }

    fun serialize() {
        DataStore.editingId = id
        DataStore.privateStore.putString(Key.name, name)

        DataStore.privateStore.putString("nodeParentName", nodeParentName)
        DataStore.privateStore.putString("nodeParentCode", nodeParentCode)
        DataStore.privateStore.putString("nodeChildName", nodeChildName)

        DataStore.privateStore.putString("pa0", pa0)
        DataStore.privateStore.putString("pa1", pa1)
        DataStore.privateStore.putString("pa2", pa2)
        DataStore.privateStore.putString("pa3", pa3)
        DataStore.privateStore.putString("pa4", pa4)
        DataStore.privateStore.putString("pa5", pa5)
        DataStore.privateStore.putString("pa6", pa6)
        DataStore.privateStore.putString("pa7", pa7)
        DataStore.privateStore.putString("pa8", pa8)
        DataStore.privateStore.putString("pa9", pa9)
        DataStore.privateStore.putString("pa10", pa10)

        DataStore.privateStore.putString(Key.host, host)
        DataStore.privateStore.putString(Key.remotePort, remotePort.toString())
        DataStore.privateStore.putString(Key.password, password)
        DataStore.privateStore.putString(Key.route, route)
        DataStore.privateStore.putString("nodeInFast", nodeInFast)
        DataStore.privateStore.putString(Key.remoteDns, remoteDns)
        DataStore.privateStore.putString(Key.method, method)
        DataStore.proxyApps = proxyApps
        DataStore.bypass = bypass
        DataStore.privateStore.putBoolean(Key.udpdns, udpdns)
        DataStore.privateStore.putBoolean(Key.ipv6, ipv6)
        DataStore.privateStore.putBoolean(Key.metered, metered)
        DataStore.individual = individual
        DataStore.plugin = plugin ?: ""
        DataStore.udpFallback = udpFallback
        DataStore.privateStore.remove(Key.dirty)
    }

    fun deserialize() {
        check(id == 0L || DataStore.editingId == id)
        DataStore.editingId = null
        // It's assumed that default values are never used, so 0/false/null is always used even if that isn't the case
        name = DataStore.privateStore.getString(Key.name) ?: ""
        nodeParentName = DataStore.privateStore.getString("nodeParentName") ?: ""
        nodeParentCode = DataStore.privateStore.getString("nodeParentCode") ?: ""
        nodeChildName = DataStore.privateStore.getString("nodeChildName") ?: ""

        pa0 = DataStore.privateStore.getString("pa0") ?: ""
        pa1 = DataStore.privateStore.getString("pa1") ?: ""
        pa2 = DataStore.privateStore.getString("pa2") ?: ""
        pa3 = DataStore.privateStore.getString("pa3") ?: ""
        pa4 = DataStore.privateStore.getString("pa4") ?: ""
        pa5 = DataStore.privateStore.getString("pa5") ?: ""
        pa6 = DataStore.privateStore.getString("pa6") ?: ""
        pa7 = DataStore.privateStore.getString("pa7") ?: ""
        pa8 = DataStore.privateStore.getString("pa8") ?: ""
        pa9 = DataStore.privateStore.getString("pa9") ?: ""
        pa10 = DataStore.privateStore.getString("pa10") ?: ""

        // It's safe to trim the hostname, as we expect no leading or trailing whitespaces here
        host = (DataStore.privateStore.getString(Key.host) ?: "").trim()
        remotePort = parsePort(DataStore.privateStore.getString(Key.remotePort), 8388, 1)
        password = DataStore.privateStore.getString(Key.password) ?: ""
        method = DataStore.privateStore.getString(Key.method) ?: ""
        route = DataStore.privateStore.getString(Key.route) ?: ""
        remoteDns = DataStore.privateStore.getString(Key.remoteDns) ?: ""
        proxyApps = DataStore.proxyApps
        bypass = DataStore.bypass
        udpdns = DataStore.privateStore.getBoolean(Key.udpdns, false)
        ipv6 = DataStore.privateStore.getBoolean(Key.ipv6, false)
        metered = DataStore.privateStore.getBoolean(Key.metered, false)
        nodeInFast = DataStore.privateStore.getString("nodeInFast", "")?:""
        individual = DataStore.individual
        plugin = DataStore.plugin
        udpFallback = DataStore.udpFallback
    }
}
