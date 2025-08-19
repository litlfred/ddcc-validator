# Digital Health Certificate Data Models

This project supports multiple digital health certificate standards. Each standard is implemented as a data model in the codebase, with mapping logic to FHIR and other healthcare formats. Below, you'll find an overview of the core models, their relationships, and direct links to their source code.

---

## 1. EU DCC (EU Digital COVID Certificate)

- **Format:** HCERT/CBOR
- **Description:** The EU DCC is a digital certificate standard developed by the European Union for cross-border COVID-19 credential verification. It uses the HCERT container format.
- **Core Model:** `CWT` → `HCERT` → `EUDCC`, `PersonName`, `Vaccination`, `Test`, `Recovery`
- **Source Code:**  
  - [DccModel.kt](../verify/src/main/java/org/who/ddccverifier/verify/hcert/dcc/DccModel.kt)
  - [DccMapper.kt](../verify/src/main/java/org/who/ddccverifier/verify/hcert/dcc/DccMapper.kt)

---

## 2. WHO DDCC (Digital Documentation of COVID-19 Certificates)

- **Format:** HCERT/CBOR (WHO extensions)
- **Description:** WHO's DDCC models both vaccination and testing events. It extends the HCERT envelope with custom payloads for vaccination (`WHO_CoreDataSet_VS`) and testing (`WHO_CoreDataSet_TR`).
- **Core Model:** `WHO_CoreDataSet`, `WHO_Certificate`, `WHO_Vaccination`, `WHO_Test`
- **Source Code:**  
  - [WhoModel.kt](../verify/src/main/java/org/who/ddccverifier/verify/hcert/who/WhoModel.kt)

---

## 3. SHC (SMART Health Card)

- **Format:** JWT/JSON
- **Description:** SHC is a North American digital health certificate standard using JWT and FHIR bundles.
- **Core Model:** `JWTPayload`, `VC`, `CredentialSubject`
- **Source Code:**  
  - [ShcModel.kt](../verify/src/main/java/org/who/ddccverifier/verify/shc/ShcModel.kt)

---

## 4. DIVOC (India Digital Certificate)

- **Format:** JSON-LD / W3C Verifiable Credential
- **Description:** DIVOC uses the W3C Verifiable Credential standard to represent vaccination and test credentials.
- **Core Model:** `W3CVC`, `CredentialSubject`, `Evidence`, `Proof`, `Verifier`, `Facility`
- **Source Code:**  
  - [DivocModel.kt](../verify/src/main/java/org/who/ddccverifier/verify/divoc/DivocModel.kt)

---

## 5. ICAO

- **Format:** iJSON
- **Description:** ICAO's model is used for test certificates, mainly for international air travel.
- **Core Model:** `DateTimeTestReport`, `TestResult`, `Signature`
- **Source Code:**  
  - [IcaoModel.kt](../verify/src/main/java/org/who/ddccverifier/verify/icao/IcaoModel.kt)

---

## 6. Common Infrastructure

- **Base Class:** All models inherit from [`BaseModel`](../verify/src/main/java/org/who/ddccverifier/verify/BaseModel.kt)
- **Verifiers:**  
  - Main entry point is [`HCertVerifier`](../verify/src/main/java/org/who/ddccverifier/verify/hcert/HCertVerifier.kt), which routes to the appropriate mapper based on certificate type.

---

## Relationships

- All models are ultimately mapped into [FHIR](https://www.hl7.org/fhir/) objects for interoperability.
- The [README](../README.md) contains a class diagram and verification flow.
- For workflow and usage, see [User Workflows](./user-workflows.md).

---

## External Standards

- [EU DCC Technical Specifications](https://ec.europa.eu/health/sites/default/files/ehealth/docs/digital-green-certificates_v1_en.pdf)
- [WHO DDCC:VS Specification](https://www.who.int/publications/i/item/WHO-2019-nCoV-Digital_certificates-vaccination-2021.1)
- [SMART Health Card](https://spec.smarthealth.cards/)
- [DIVOC Documentation](https://divoc.digit.org/)
- [ICAO Visible Digital Seal](https://www.icao.int/Security/FAL/TRIP/Pages/Visible-Digital-Seals.aspx)

---

## See Also

- [User Workflows](./user-workflows.md)
- [README Class Diagram](../README.md)

---