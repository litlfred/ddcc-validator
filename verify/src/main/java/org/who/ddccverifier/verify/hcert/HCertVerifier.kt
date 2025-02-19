package org.who.ddccverifier.verify.hcert

import COSE.MessageTag
import COSE.OneKey
import COSE.Sign1Message
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.upokecenter.cbor.CBORObject
import nl.minvws.encoding.Base45
import org.hl7.fhir.r4.model.Bundle
import org.who.ddccverifier.QRDecoder
import org.who.ddccverifier.trust.TrustRegistry
import org.who.ddccverifier.verify.hcert.dcc.DccMapper
import org.who.ddccverifier.verify.hcert.dcc.logical.CWT
import org.who.ddccverifier.verify.hcert.dcc.logical.WHO_CoreDataSet
import org.who.ddccverifier.verify.hcert.who.WhoMapper
import java.security.PublicKey
import java.util.*
import java.util.zip.InflaterInputStream
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Turns HC1 QR Codes into Fhir Objects
 */
class HCertVerifier (private val registry: TrustRegistry) {
    private val prefix = "HC1:"

    private fun prefixDecode(qr: String): String {
        return when {
            qr.startsWith(prefix) -> qr.drop(prefix.length)
            else -> qr
        }
    }

    private fun base45Decode(base45: String): ByteArray? {
        return try {
            Base45.getDecoder().decode(base45)
        } catch (e: Throwable) {
            null
        }
    }

    private fun deflate(input: ByteArray): ByteArray? {
        return try {
            InflaterInputStream(input.inputStream()).readBytes()
        } catch (e: Throwable) {
            null
        }
    }

    private fun decodeSignedMessage(input: ByteArray): Sign1Message? {
        return try {
            Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message
        } catch (e: Throwable) {
            null
        }
    }

    val ISS_CODE = 1
    private fun getKID(input: Sign1Message): String? {
        var kid = input.protectedAttributes[COSE.HeaderKeys.KID.AsCBOR()]?.GetByteString()
            ?: input.unprotectedAttributes[COSE.HeaderKeys.KID.AsCBOR()]?.GetByteString()
            ?: return null
        if (input.unprotectedAttributes[ISS_CODE] != null)
            kid = input.unprotectedAttributes[ISS_CODE].GetByteString() + kid
        return Base64.getEncoder().encodeToString(kid)
    }

    private fun resolveIssuer(kid: String): TrustRegistry.TrustedEntity? {
        return registry.resolve(TrustRegistry.Framework.DCC, kid)
    }

    private fun getContent(signedMessage: Sign1Message): CBORObject {
        return CBORObject.DecodeFromBytes(signedMessage.GetContent())
    }

    @OptIn(ExperimentalTime::class)
    private fun verify(signedMessage: Sign1Message, pubKey: PublicKey): Boolean {
        return try {
            val (verified, elapsedStructureMapLoad) = measureTimedValue {
                val key = OneKey(pubKey, null)
                signedMessage.validate(key)
            }
            println("TIME: Verify $elapsedStructureMapLoad")

            return verified
        } catch (e: Throwable) {
            false
        }
    }

    fun unpack(qr: String): CBORObject? {
        val hc1Decoded = prefixDecode(qr)
        val decodedBytes = base45Decode(hc1Decoded) ?: return null
        val deflatedBytes = deflate(decodedBytes) ?: return null
        val signedMessage = decodeSignedMessage(deflatedBytes) ?: return null
        return getContent(signedMessage)
    }

    val HCERT_CODE = -260
    val DCC_CODE = 1
    val DDCC_VS_CODE = 2
    val DDCC_TR_CODE = 3

    fun toFhir(hcertPayload: CBORObject): Bundle? {
        val hcertClaim = hcertPayload[HCERT_CODE]
        if (hcertClaim == null) {
            println("No HCERT Code")
            return null
        }

        if (hcertClaim[DCC_CODE] != null) 
            try {
                 println("Found DCC Code")
                 return DccMapper().run(
                    jacksonObjectMapper().readValue(
                    hcertPayload.ToJSONString(),
                            CWT::class.java
                        )
                    )
                } catch (e: Exception) {
                    println("Exception in mapping DCC")
                    e.printStackTrace()
                }

        if (hcertClaim[DDCC_VS_CODE] != null
            || hcertClaim[DDCC_TR_CODE] != null )
            try {
                println("Found VS or TR code")
                return WhoMapper().run(
                    jacksonObjectMapper().readValue(
                        hcertPayload.ToJSONString(),
                        CWT::class.java
                    )
                )
            } catch (e: Exception) {
                println("Exception in processing DDCC")
                 e.printStackTrace()
            }

        println("no valid claim under hcert")
        println(hcertPayload.ToJSONString())
        return null
                
    }
    

    fun unpackAndVerify(qr: String): QRDecoder.VerificationResult {
        val hc1Decoded = prefixDecode(qr)
        val decodedBytes = base45Decode(hc1Decoded) ?: return QRDecoder.VerificationResult(QRDecoder.Status.INVALID_ENCODING, null, null, qr, null)
        val deflatedBytes = deflate(decodedBytes) ?: return QRDecoder.VerificationResult(QRDecoder.Status.INVALID_COMPRESSION, null, null, qr, null)
        val signedMessage = decodeSignedMessage(deflatedBytes) ?: return QRDecoder.VerificationResult(
            QRDecoder.Status.INVALID_SIGNING_FORMAT, null, null, qr, null)

        val contentsCBOR = getContent(signedMessage)
        val unpacked = contentsCBOR.ToJSONString()

        val contents = toFhir(contentsCBOR) ?: return QRDecoder.VerificationResult(QRDecoder.Status.NOT_SUPPORTED, null, null, qr, unpacked)

        val kid = getKID(signedMessage) ?: return QRDecoder.VerificationResult(QRDecoder.Status.KID_NOT_INCLUDED, contents, null, qr, unpacked)
        val issuer = resolveIssuer(kid) ?: return QRDecoder.VerificationResult(QRDecoder.Status.ISSUER_NOT_TRUSTED, contents, null, qr, unpacked)

        return when (issuer.status) {
            TrustRegistry.Status.TERMINATED -> QRDecoder.VerificationResult(QRDecoder.Status.TERMINATED_KEYS, contents, issuer, qr, unpacked)
            TrustRegistry.Status.EXPIRED -> QRDecoder.VerificationResult(QRDecoder.Status.EXPIRED_KEYS, contents, issuer, qr, unpacked)
            TrustRegistry.Status.REVOKED -> QRDecoder.VerificationResult(QRDecoder.Status.REVOKED_KEYS, contents, issuer, qr, unpacked)
            TrustRegistry.Status.CURRENT ->
                if (verify(signedMessage, issuer.publicKey))
                    QRDecoder.VerificationResult(QRDecoder.Status.VERIFIED, contents, issuer, qr, unpacked)
                else
                    QRDecoder.VerificationResult(QRDecoder.Status.INVALID_SIGNATURE, contents, issuer, qr, unpacked)
        }
    }
}
