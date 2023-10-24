package org.who.ddccverifier.verify.hcert.dcc.logical

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
//    val identifier: Identifier?,
//    val certifcate: WHO_Certificate?,
): BaseModel()

class WHO_CoreDataSet_VS (
    val vaccination: WHO_Vaccination?,  name: StringType?,birthDate: DateType?,
//    val identifier: Identifier?,
//    val certificate: WHO_Certificate?
//): WHO_CoreDataSet(name, birthDate, identifier, certificate)
): WHO_CoreDataSet(name, birthDate)

class WHO_CoreDataSet_TR (
    val test: WHO_Test?, name: StringType?, birthDate: DateType?,
//    val identifier: Identifier?,
//    val certificate: WHO_Certificate?
//): WHO_CoreDataSet(name, birthDate, identifier, certificate)
): WHO_CoreDataSet(name, birthDate)

class WHO_Certificate(
    val issuer: Reference, //is there a way to specify it is Organization Reference?
    val kid: StringType?,
    val hcid: Identifier?,
    val ddccid: Identifier?,
    val version: StringType,
    val period: WHO_CertificatePeriod?
): BaseModel()

class WHO_CertificatePeriod(
    val start: DateTimeType?,
    val end: DateTimeType?,
): BaseModel()

class WHO_Vaccination(
//    val vaccine: Coding?,
//    val brand: Coding?,
//    val manufacturer: Coding?,
//    val maholder: Coding?,
//    val lot: StringType?,
//    val date: DateTimeType?,
//    val validFrom: DateType?,
//    val dose: PositiveIntType?,
//    val totalDoses: PositiveIntType?,
//    val country: Coding?,
//    val centre: StringType?,
//    val signature: Signature?,
//    val practitioner: Identifier?,
//    val disease: Coding?,
//    val nextDose: DateTimeType?
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
