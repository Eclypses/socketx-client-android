package com.eclypses.socketx_client_android

import android.util.Base64
import com.eclypses.mte.MteBase
import com.eclypses.mte.MteKyber
import com.eclypses.mte.MteMkeDec
import com.eclypses.mte.MteMkeEnc
import com.eclypses.mte.MteStatus
import org.json.JSONObject
import java.math.BigInteger

class MteCodec {

    private var encPersStr: String
    private var decPersStr: String
    private var encKyber: MteKyber
    private var decKyber: MteKyber

    private var encMyPublicKey: ByteArray
    private lateinit var encPeerEncryptedSecret: ByteArray
    private var decMyPublicKey: ByteArray
    private lateinit var decPeerEncryptedSecret: ByteArray
    private var encNonce: Long = 0
    private var decNonce: Long = 0
    private lateinit var encoder: MteMkeEnc
    private lateinit var decoder: MteMkeDec

    init {
        try {
            MteKyber.init(MteKyber.KyberStrength.K512)

            encPersStr = getRandomString(64)
            decPersStr = getRandomString(64)
            val publicKeySize = MteKyber.getPublicKeySize()
            encMyPublicKey = ByteArray(publicKeySize)
            decMyPublicKey = ByteArray(publicKeySize)

            encKyber = MteKyber()
            val encKyberStatus = encKyber.createKeyPair(encMyPublicKey)
            checkKyberStatus(encKyberStatus, "encKyber.createKeyPair")

            decKyber = MteKyber()
            val decKyberStatus = decKyber.createKeyPair(decMyPublicKey)
            checkKyberStatus(decKyberStatus, "decKyber.createKeyPair")

        } catch (e: Exception) {
            throw SocketXError.CodecError("Failed to initialize MteCodec: ${e.message}")
        }
    }

    fun getPairingKeys(): ByteArray {
        val json = JSONObject()
        json.put("encoderPublicKey", Base64.encodeToString(encMyPublicKey, Base64.NO_WRAP))
        json.put("encoderPersonalization", encPersStr)
        json.put("decoderPublicKey", Base64.encodeToString(decMyPublicKey, Base64.NO_WRAP))
        json.put("decoderPersonalization", decPersStr)
        return json.toString().toByteArray(Charsets.UTF_8)
    }

    fun completePairing(payload: ByteArray) {
        try {
            val jsonString = payload.toString(Charsets.UTF_8)
//            Log.d("MTE", "Received pairing payload: $jsonString")
            val jsonObj = JSONObject(jsonString)
            encPeerEncryptedSecret = Base64.decode(jsonObj.getString("decoderSecret"), Base64.NO_WRAP)
            // Use BigInteger to correctly parse large ULong strings
            encNonce = BigInteger(jsonObj.getString("decoderNonce")).toLong()
            decPeerEncryptedSecret = Base64.decode(jsonObj.getString("encoderSecret"), Base64.NO_WRAP)
            decNonce = BigInteger(jsonObj.getString("encoderNonce")).toLong()
            createEncoderAndDecoder()
        } catch (e: Exception) {
            throw SocketXError.CodecError("Failed to complete pairing: ${e.message}")
        }
    }

    private fun createEncoderAndDecoder() {
        instantiateEncoder()
        instantiateDecoder()
    }

    private fun instantiateEncoder() {
        encoder = MteMkeEnc()
        val encSecret = ByteArray(MteKyber.getSecretSize())
        val decryptSecretStatus = encKyber.decryptSecret(encPeerEncryptedSecret, encSecret)
        checkKyberStatus(decryptSecretStatus, "encKyber.decryptSecret")
        encoder.setEntropy(encSecret);
        encoder.setNonce(encNonce);
        val initStatus = encoder.instantiate(encPersStr)
        checkMteStatus("instantiateEncoder", initStatus)
    }

    private fun instantiateDecoder() {
        decoder = MteMkeDec()
        val decSecret = ByteArray(MteKyber.getSecretSize())
        val decryptSecretStatus = decKyber.decryptSecret(decPeerEncryptedSecret, decSecret)
        checkKyberStatus(decryptSecretStatus, "decKyber.decryptSecret")
        decoder.setEntropy(decSecret);
        decoder.setNonce(decNonce);
        val initStatus = decoder.instantiate(decPersStr)
        checkMteStatus("instantiateDecoder", initStatus)
    }

    fun encode(data: ByteArray): ByteArray {
        val encodeResult = encoder.encode(data)
        checkMteStatus("encode", encodeResult.status)
        return encodeResult.arr
    }

    fun decode(data: ByteArray): ByteArray {
        val decodeResult = decoder.decode(data)
        checkMteStatus("decode", decodeResult.status)
        return decodeResult.arr
    }

    private fun checkMteStatus(function: String, status: MteStatus) {
        if (status != MteStatus.mte_status_success) {
            throw SocketXError.CodecError("$function failed: ${MteBase.getStatusName(status)} - ${MteBase.getStatusDescription(status)}")
        }
    }

    private fun checkKyberStatus(status: Int, function: String) {
        if (status != MteKyber.Success) {
            throw SocketXError.InternalError("$function failed with Kyber error code: $status")
        }
    }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }


}
