/**
* OPAL Program
*
* MODULE      : alter_pdpo_identifier_type_enum_add_defendant.sql
*
* DESCRIPTION : Alter PDPO identifier individual type enum to add defendant 
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 14/10/2026    P Brumby       1.0         PO-10996 - Add defendant to PDPO identifier individual type enum
*
**/

ALTER TYPE t_pdpo_identifier_individual_type_enum
    ADD VALUE IF NOT EXISTS 'DEFENDANT';
