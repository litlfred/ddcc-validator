# User Workflows and Developer Orientation

This guide describes main user workflows and provides orientation for developers aiming to add new certificate models or new model versions to the system. It ties together user experience and backend code structure, so new contributors can quickly understand where to integrate new logic.

See also: [Digital Health Certificate Data Models](./data-models.md)

---

## Workflow Overview

### 1. Scanning and Decoding a Health Certificate

**User Action:**  
- The user scans a QR code or uploads a digital certificate file.

**System Flow:**  
1. **Decoding:**  
   - The QR or file input is decoded according to format (e.g., Base45, CBOR, JWT, JSON-LD).
   - *Key Entry Point:*  
     - [HCertVerifier.kt](../verify/src/main/java/org/who/ddccverifier/verify/hcert/HCertVerifier.kt) for HCERT (EU DCC, WHO DDCC)
     - [ShcMapper.kt](../verify/src/main/java/org/who/ddccverifier/verify/shc/ShcMapper.kt) for SHC
     - [DivocMapper.kt](../verify/src/main/java/org/who/ddccverifier/verify/divoc/DivocMapper.kt) for DIVOC
     - [IcaoMapper.kt](../verify/src/main/java/org/who/ddccverifier/verify/icao/IcaoMapper.kt) for ICAO

2. **Model Detection and Instantiation:**  
   - The payload format is detected (HCERT, SHC, etc.)
   - The payload is deserialized into the corresponding model:
     - **EU DCC/WHO DDCC:** `CWT` → `HCERT` → `EUDCC` or `WHO_CoreDataSet_*`
     - **SHC:** `JWTPayload`
     - **DIVOC:** `W3CVC`
     - **ICAO:** `IJson`
   - *Model definitions are found in [`verify/src/main/java/org/who/ddccverifier/verify/`](../verify/src/main/java/org/who/ddccverifier/verify/)* under folders for each standard.

---

### 2. Verification

**User Action:**  
- The user expects verification of authenticity and trust.

**System Flow:**  
1. **Signature Verification:**  
   - Each model type has a specific cryptographic verification step (COSE, JWT, etc.).
   - Trust is validated via a trusted issuer registry ([TrustRegistry](../verify/src/main/java/org/who/ddccverifier/trust/TrustRegistry.kt)).
2. **Error Handling:**  
   - If verification fails, the user is notified and the workflow ends.
   - If successful, proceed to transformation.

---

### 3. Data Transformation and Assessment

**System Flow:**  
1. **Mapping to FHIR:**  
   - The model is mapped to a FHIR `Bundle` using a specific structure map (e.g., `EUDCCtoDDCC.map`, `WHOtoDDCC.map`).
   - *Transformation logic lives in Mapper classes, e.g., [`DccMapper.kt`](../verify/src/main/java/org/who/ddccverifier/verify/hcert/dcc/DccMapper.kt).*
2. **Assessment:**  
   - CQL rules may be evaluated against the FHIR bundle for health status (e.g., vaccinated, tested).

---

### 4. Display

**System Flow:**  
- The app presents health data, credential metadata, assessment, and issuer info to the user.

---

## Developer Guide: Adding a New Model or Version

### 1. Identify the Standard & Data Format

- Determine if the standard uses CBOR/HCERT, JWT, JSON-LD, or iJSON.
- Review similar models (see [Digital Health Certificate Data Models](./data-models.md)).

### 2. Add Model Classes

- Create a new model class (or version) in the appropriate directory:
  - Example: `verify/src/main/java/org/who/ddccverifier/verify/hcert/newmodel/NewModel.kt`
- Inherit from `BaseModel` for consistency.
- Use Jackson annotations for JSON/CBOR mapping as needed.

### 3. Add a Mapper

- Create a Mapper class (e.g., `NewModelMapper.kt`) extending `BaseMapper`.
- Implement a `run()` method that takes your new model and returns a FHIR `Bundle`.
- Reference or create a structure map file for transforming your model to FHIR.

### 4. Update the Verifier

- If your model is a new type, update the relevant verifier (`HCertVerifier.kt`, etc.) to detect and route your model.
- Add detection logic to select your Mapper and parse your model from the payload.

### 5. Update Documentation

- Add your new model to:
  - [Digital Health Certificate Data Models](./data-models.md)
  - This workflow document (user-workflows.md)
- Include a summary, fields, and source code links.

### 6. Testing

- Add unit tests for model parsing, mapping, and verification.
- Test edge cases (signature errors, malformed payloads).

---

## Example: Adding "MyNewDCC"

1. **Create Model:**  
   - `MyNewDccModel.kt` in a subpackage.
2. **Create Mapper:**  
   - `MyNewDccMapper.kt` with a `run(model: MyNewDcc): Bundle` method.
3. **Hook Up Detection:**  
   - Update `HCertVerifier.kt` to detect and process MyNewDCC QR codes.
4. **Document:**  
   - Add to [Digital Health Certificate Data Models](./data-models.md) and here.
5. **Test:**  
   - Add tests for signature validation, mapping to FHIR, and error handling.

---

## Visual Workflow

```
User → [Scan/Upload Certificate]
     → [Decode & Detect Model Type]
         → [Instantiate Model Class]
             → [Verify Signature & Trust]
                 → [Map to FHIR Bundle]
                     → [Assessment (CQL)]
                         → [Display Results/Status]
```

---

## Quick Links

- [Digital Health Certificate Data Models](./data-models.md)
- [FHIR Structure Maps](https://worldhealthorganization.github.io/ddcc/)
- [Trust Verification](https://github.com/WorldHealthOrganization/ddcc-trust)
- [Main App Diagram](../README.md)