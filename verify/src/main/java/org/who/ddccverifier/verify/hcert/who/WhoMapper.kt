package org.who.ddccverifier.verify.hcert.who

import org.hl7.fhir.r4.model.Bundle
import org.who.ddccverifier.verify.BaseMapper
import org.who.ddccverifier.verify.hcert.dcc.logical.WHO_CoreDataSet

/**
 * Translates a DDCC QR CBOR object into FHIR Objects
 */
class WhoMapper: BaseMapper() {
    fun run(who: WHO_CoreDataSet): Bundle {
        return super.run(
            who,
            "WHOtoDDCC.map"
        )
    }
}
