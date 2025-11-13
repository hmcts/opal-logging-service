/**
* OPAL Program
*
* MODULE      : create_pdpo_identifiers_indexes.sql
*
* DESCRIPTION : Create indexes on pdpo_identifiers table for performance
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/11/2025    C Cho       1.0         PO-2271 Create indexes on pdpo_identifiers table for performance
*
**/

-- Create performance indexes on pdpo_identifiers table
CREATE INDEX pdpo_identifiers_business_idx 
    ON pdpo_identifiers (business_identifier);