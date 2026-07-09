/**
* OPAL Program
*
* MODULE      : add_unique_constraint_to_pdpo_identifiers_business_identifier.sql
*
* DESCRIPTION : Add a unique constraint to pdpo_identifiers.business_identifier
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------------------------
* 22/06/2026    C Cho       1.0         PO-7265 - Add a unique constraint to pdpo_identifiers.business_identifier
*
**/


DROP INDEX IF EXISTS public.pdpo_identifiers_business_idx;

ALTER TABLE ONLY public.pdpo_identifiers
    ADD CONSTRAINT pdpo_identifiers_business_identifier_uk UNIQUE (business_identifier);
