/**
* OPAL Program
*
* MODULE      : create_pdpo_identifiers_sequence.sql
*
* DESCRIPTION : Create sequence for pdpo_identifiers table primary key
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/11/2025    C Cho       1.0         PO-2271 Create sequence for pdpo_identifiers table primary key
*
**/

-- Create sequence for pdpo_identifiers_id primary key
CREATE SEQUENCE pdpo_identifiers_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1
    OWNED BY pdpo_identifiers.pdpo_identifiers_id;