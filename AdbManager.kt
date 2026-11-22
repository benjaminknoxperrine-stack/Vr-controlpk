package com.example.questcontrol

import android.content.Context
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date

/** Holds this app's ADB identity (RSA key + cert) and runs shell commands. */
class AdbManager(ctx: Context) : AbsAdbConnectionManager() {
    private val privateKey: PrivateKey
    private val cert: Certificate

    init {
        val kf = File(ctx.filesDir, "adb_key.pk8")
        val cf = File(ctx.filesDir, "adb_cert.der")
        if (!kf.exists() || !cf.exists()) {
            val kp = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
            val name = X500Name("CN=QuestControl")
            val now = Date()
            val builder = JcaX509v3CertificateBuilder(
                name, BigInteger.valueOf(now.time), now,
                Date(now.time + 10L * 365 * 24 * 3600 * 1000), name, kp.public
            )
            val signer = JcaContentSignerBuilder("SHA256withRSA").build(kp.private)
            val c = JcaX509CertificateConverter().getCertificate(builder.build(signer))
            kf.writeBytes(kp.private.encoded)
            cf.writeBytes(c.encoded)
        }
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(kf.readBytes()))
        cert = CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(cf.readBytes()))
    }

    override fun getPrivateKey(): PrivateKey = privateKey
    override fun getCertificate(): Certificate = cert
    override fun getDeviceName(): String = "QuestControl"

    fun run(cmd: String): String =
        openStream("shell:$cmd").use { it.openInputStream().bufferedReader().readText().trim() }
}
