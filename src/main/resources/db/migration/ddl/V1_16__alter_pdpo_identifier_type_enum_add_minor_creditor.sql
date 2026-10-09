/**
* OPAL Program
*
* MODULE      : alter_pdpo_identifier_type_enum_add_minor_creditor.sql
*
* DESCRIPTION : Alter PDPO identifier type enum to add minor creditor
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 01/10/2026    P Brumby       1.0         PO-10727 - Add minor creditor to PDPO identifier type enum
*
**/

ALTER TYPE t_pdpo_identifier_individual_type_enum
    ADD VALUE IF NOT EXISTS 'MINOR_CREDITOR';
