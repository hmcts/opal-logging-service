/**
* OPAL Program
*
* MODULE      : create_pdpo_log_indexes.sql
*
* DESCRIPTION : Create indexes on pdpo_log table for performance
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/11/2025    C Cho       1.0         PO-2247 Create indexes on pdpo_log table for performance
*
**/

-- Create performance indexes on pdpo_log table
CREATE INDEX pdpo_log_recipient_idx 
    ON pdpo_log (recipient_identifier_type, recipient_identifier);

CREATE INDEX pdpo_log_category_idx 
    ON pdpo_log (category);

CREATE INDEX pdpo_log_created_by_idx 
    ON pdpo_log (created_by_identifier_type, created_by_identifier, pdpo_identifiers_id);

CREATE INDEX pdpo_log_created_at_idx 
    ON pdpo_log (created_at);

CREATE INDEX pdpo_log_ip_address_idx 
    ON pdpo_log (ip_address);

CREATE INDEX pdpo_log_identifiers_fk_idx 
    ON pdpo_log (pdpo_identifiers_id);