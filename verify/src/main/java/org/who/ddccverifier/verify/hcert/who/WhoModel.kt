package org.who.ddccverifier.verify.hcert.who

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.*
import org.who.ddccverifier.verify.BaseModel
import org.who.ddccverifier.verify.shc.DecimalToDataTimeDeserializer
import kotlin.reflect.full.declaredMemberProperties


open class WHO_CoreDataSet (
    val name: StringType?,
    val birthDate: DateType?,
    val identifier: StringType?,
): BaseModel()
@JsonIgnoreProperties(ignoreUnknown = true)
class WHO_CoreDataSet_VS (
    val name: StringType?,
    val birthDate: DateType?,
    val sex: CodeType?,
    val identifier: StringType?,
    // certificate
    val certificate: WHO_Certificate?,
    // vaccination
    val vaccination: WHO_Vaccination?,
): BaseModel()

class WHO_CoreDataSet_TR (
    val test: WHO_Test?,
    val name: StringType?,
    val birthDate: DateType?,
    val identifier: StringType?,
    val certificate: WHO_Certificate?
): BaseModel()


class WHO_Certificate(
    val hcid: StringType?,
    val issuer: IdentifierData?, //is there a way to specify it is Organization Reference?
    val period: WHO_CertificatePeriod?,
    val kid: StringType?,
    val ddccid: IdentifierData?,
    val version: StringType,
): BaseModel()

class WHO_CertificatePeriod(
    val start: DateTimeType?,
    val end: DateTimeType?,
): BaseModel()

class IdentifierData (
    val identifier: StringType?,
): BaseModel()
class WHO_Vaccination(
    val vaccine: Coding?,
    val brand: Coding?,
    @JsonDeserialize(using = CodingOrReferenceDeserializer::class)
    val manufacturer: Base?,
    val maholder: Coding?,
    val lot: StringType?,
    val date: DateTimeType?,
    val validFrom: DateType?,
    val dose: PositiveIntType?,
    val totalDoses: PositiveIntType?,
    val country: Coding?,
    val centre: StringType?,
    //val signature: Signature?,
    val practitioner: StringType?,
    val disease: Coding?,
    val nextDose: DateTimeType,

): BaseModel() 

class WHO_Test(
    val pathogen: Coding?,
    val type: Coding?,
    val brand: Coding?,
    val manufacturer: Coding?,
    val origin: Coding?,
    val date: DateType?,
    val result: Coding?,
    val centre: Coding?,
    val country: Coding?
): BaseModel()

object CodingOrReferenceDeserializer: JsonDeserializer<Base>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Base? {
        val token: TreeNode = p.readValueAsTree()

        return if (token.isValueNode) {
            Reference().apply {
                id = token.toString()
            }
        } else {
            return jacksonObjectMapper().readValue<Coding>(token.toString())
        }
    }
}