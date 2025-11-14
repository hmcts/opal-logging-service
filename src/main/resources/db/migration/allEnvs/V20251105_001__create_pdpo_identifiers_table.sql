/**
* OPAL Program
*
* MODULE      : create_pdpo_identifiers_table.sql
*
* DESCRIPTION : Create pdpo_identifiers table in the new Logging service database
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/11/2025    C Cho       1.0         PO-2271 Create pdpo_identifiers table in the new Logging service database
*
**/

CREATE TABLE pdpo_identifiers (
    pdpo_identifiers_id     BIGINT          NOT NULL,
    business_identifier     VARCHAR(250)    NOT NULL,
    
    CONSTRAINT pdpo_identifiers_pk PRIMARY KEY (pdpo_identifiers_id)
);

COMMENT ON COLUMN pdpo_identifiers.pdpo_identifiers_id IS 'Generated primary key for the pdpo_identifiers table';
COMMENT ON COLUMN pdpo_identifiers.business_identifier IS 'A unique text representing the business identifier description of the pdpo log';