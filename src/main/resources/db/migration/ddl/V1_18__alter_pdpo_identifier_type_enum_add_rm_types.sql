/**
* OPAL Program
*
* MODULE      : alter_pdpo_identifier_type_enum_add_rm_types.sql
*
* DESCRIPTION : Add RM record identifiers to t_pdpo_identifier_individual_type_enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 22/10/2026    Tom Reed       1.0         Add RM Draft Casefile, Respondent Account and Creditor Account identifiers
*
**/

ALTER TYPE t_pdpo_identifier_individual_type_enum
    ADD VALUE IF NOT EXISTS 'RM_DRAFT_CASEFILE';

ALTER TYPE t_pdpo_identifier_individual_type_enum
    ADD VALUE IF NOT EXISTS 'RM_RESPONDENT_ACCOUNT';

ALTER TYPE t_pdpo_identifier_individual_type_enum
    ADD VALUE IF NOT EXISTS 'RM_CREDITOR_ACCOUNT';
