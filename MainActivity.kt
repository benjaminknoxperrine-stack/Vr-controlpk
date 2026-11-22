package com.example.questcontrol

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.*
import org.conscrypt.Conscrypt
import java.security.Security

class MainActivity : Activity() {
    private lateinit var adb: AdbManager
    private lateinit var log: TextView
    private lateinit var root: LinearLayout
    private lateinit var host: EditText
    private lateinit var port: EditText
    private lateinit var pairPort: EditText
    private lateinit var code: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adb = AdbManager(this)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 48, 48, 48) }
        setContentView(ScrollView(this).apply { addView(root) })

        label("Quest Control  (connect to this headset's own ADB first)")
        host = field("Host", "127.0.0.1")
        port = field("Connect port (5555 for tcpip, or the Wireless debugging port)", "5555", true)
        pairPort = field("Pairing port (Wireless debugging only)", "", true)
        code = field("Pairing code (Wireless debugging only)", "", true)
        add("Connect") {
            bg {
                if (adb.connect(host.text.toString(), port.text.toString().toInt())) "Connected" else "Connect failed"
            }
        }
        add("Pair (Wireless debugging)") {
            bg {
                Security.insertProviderAt(Conscrypt.newProvider(), 1)
                if (adb.pair(host.text.toString(), pairPort.text.toString().toInt(), code.text.toString()))
                    "Paired. Now tap Connect with the connect port." else "Pairing failed"
            }
        }

        label("Refresh rate")
        row { for (hz in listOf(72, 90, 120)) add("$hz Hz") { prop("debug.oculus.refreshRate", hz) } }

        label("Render resolution (per eye)")
        row {
            add("Low 1440x1536") { res(1440, 1536) }
            add("Default 1832x1920") { res(1832, 1920) }
            add("High 2064x2160") { res(2064, 2160) }
        }

        label("CPU / GPU level")
        row { for (l in listOf(0, 2, 4)) add("Level $l") { prop("debug.oculus.cpuLevel", l); prop("debug.oculus.gpuLevel", l) } }

        label("Foveated rendering")
        row {
            add("Off") { prop("debug.oculus.foveation.level", 0) }
            add("Medium") { prop("debug.oculus.foveation.level", 2) }
            add("High + dynamic") { prop("debug.oculus.foveation.level", 4); prop("debug.oculus.foveation.dynamic", 1) }
        }

        label("Guardian / sensors")
        row {
            add("Guardian OFF") { prop("debug.oculus.guardian_pause", 1) }
            add("Guardian ON") { prop("debug.oculus.guardian_pause", 0) }
        }
        row {
            add("Proximity OFF") {
                sh("am broadcast -a com.oculus.vrpowermanager.prox_close")
                sh("am broadcast -a com.oculus.vrpowermanager.automation_disable")
            }
            add("Proximity ON") { sh("am broadcast -a com.oculus.vrpowermanager.automation_enable") }
        }

        label("Media volume")
        row { for (v in listOf(4, 8, 12)) add("Vol $v") { sh("media volume --stream 3 --set $v") } }

        label("Other")
        row {
            add("Battery") { sh("dumpsys battery") }
            add("Installed apps") { sh("pm list packages -3") }
            add("Reset tweaks") {
                for (k in listOf("refreshRate", "textureWidth", "textureHeight", "cpuLevel", "gpuLevel",
                    "foveation.level", "foveation.dynamic", "guardian_pause")) prop("debug.oculus.$k", "''")
            }
            add("Reboot") { sh("reboot") }
        }

        val raw = field("Raw shell command", "")
        add("Run command") { sh(raw.text.toString()) }

        log = TextView(this).apply { setTextIsSelectable(true); text = "Log:" }
        root.addView(log)
    }

    private fun res(w: Int, h: Int) { prop("debug.oculus.textureWidth", w); prop("debug.oculus.textureHeight", h) }
    private fun prop(k: String, v: Any) = sh("setprop $k $v")
    private fun sh(c: String) = bg { adb.run(c).ifBlank { "ok: $c" } }

    private fun bg(f: () -> String) = Thread {
        val r = try { f() } catch (e: Throwable) { "Error: ${e.message}" }
        runOnUiThread { log.append("\n$r") }
    }.start()

    private fun label(t: String) = root.addView(TextView(this).apply { text = t; textSize = 18f; setPadding(0, 32, 0, 8) })

    private fun field(hint: String, def: String, numeric: Boolean = false) = EditText(this).apply {
        this.hint = hint; setText(def)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER
        root.addView(this)
    }

    private var currentRow: LinearLayout? = null
    private fun row(block: () -> Unit) {
        currentRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(currentRow); block(); currentRow = null
    }
    private fun add(label: String, onClick: () -> Unit) {
        val b = Button(this).apply { text = label; setOnClickListener { onClick() } }
        (currentRow ?: root).addView(b)
    }
}
