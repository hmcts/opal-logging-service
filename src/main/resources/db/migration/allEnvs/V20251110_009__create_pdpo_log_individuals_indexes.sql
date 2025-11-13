/**
* CGI OPAL Program
*
* NAME        : create_pdpo_log_individuals_indexes.sql
*
* DESCRIPTION : Creates the indexes used for the pdpo_log_individuals table in the Logging service database
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 07/11/2025    CL          1.0         PO-2270 - Creates indexes for pdpo_log_individuals to support query performance
*                                       
*
**/

-- Foreign Key Index
CREATE INDEX pli_pdpo_log_id_fk_idx
    ON pdpo_log_individuals (pdpo_log_id);

CREATE INDEX pli_type_identifier_idx 
    ON pdpo_log_individuals (individual_type, individual_identifier);