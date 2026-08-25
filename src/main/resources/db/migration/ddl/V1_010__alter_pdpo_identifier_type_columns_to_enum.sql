/**
* OPAL Program
*
* MODULE      : alter_pdpo_identifier_type_columns_to_enum.sql
*
* DESCRIPTION : Alter PDPO identifier type columns to enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 24/08/2026    C Cho          1.0         PO-8971 - Alter PDPO identifier type columns to enum
*
**/

CREATE TYPE t_pdpo_identifier_individual_type_enum AS ENUM (
    'DEFENDANT_ACCOUNT',
    'CONSOLIDATED_ACCOUNT',
    'DEBTOR_ACCOUNT',
    'PARENT_GUARDIAN',
    'DRAFT_ACCOUNT',
    'OPAL_USER_ID',
    'EXTERNAL_SYSTEM'
);

ALTER TABLE pdpo_log
    ALTER COLUMN created_by_identifier_type TYPE t_pdpo_identifier_individual_type_enum
    USING created_by_identifier_type::t_pdpo_identifier_individual_type_enum;

ALTER TABLE pdpo_log
    ALTER COLUMN recipient_identifier_type TYPE t_pdpo_identifier_individual_type_enum
    USING recipient_identifier_type::t_pdpo_identifier_individual_type_enum;

COMMENT ON COLUMN pdpo_log.created_by_identifier_type IS 'The type of data the created_by_identifier relates to. Specific values can be found in the user defined type t_pdpo_identifier_individual_type_enum in the DB LLD on Confluence.';

COMMENT ON COLUMN pdpo_log.recipient_identifier_type IS 'The identifier type of the recipient. Specific values can be found in the user defined type t_pdpo_identifier_individual_type_enum in the DB LLD on Confluence.';

ALTER TABLE pdpo_log_individuals
    ALTER COLUMN individual_type TYPE t_pdpo_identifier_individual_type_enum
    USING individual_type::t_pdpo_identifier_individual_type_enum;

COMMENT ON COLUMN pdpo_log_individuals.individual_type IS 'The identifier type of the individual whose data was subject to a PDPO. Specific values can be found in the user defined type t_pdpo_identifier_individual_type_enum in the DB LLD on Confluence.';

