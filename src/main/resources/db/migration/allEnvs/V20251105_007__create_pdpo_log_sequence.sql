/**
* OPAL Program
*
* MODULE      : create_pdpo_log_sequence.sql
*
* DESCRIPTION : Create sequence for pdpo_log table primary key
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/11/2025    C Cho       1.0         PO-2247 Create sequence for pdpo_log table primary key
*
**/

-- Create sequence for pdpo_log_id primary key
CREATE SEQUENCE IF NOT EXISTS pdpo_log_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1
    OWNED BY pdpo_log.pdpo_log_id;