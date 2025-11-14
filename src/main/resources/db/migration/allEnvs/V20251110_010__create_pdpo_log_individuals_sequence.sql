/**
* CGI OPAL Program
*
* NAME        : create_pdpo_log_individuals_seq.sql
*
* DESCRIPTION : Creates the sequence used for the primary key of pdpo_log_individuals table in the Logging service database
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 07/11/2025    CL          1.0         PO-2270 - Creates the sequence used for the primary key of pdpo_log_individuals 
*                                       table in the Logging service database
*
**/

CREATE SEQUENCE IF NOT EXISTS pdpo_log_individuals_seq
    START WITH 1
    INCREMENT BY 1    
    CACHE 1
    OWNED BY pdpo_log_individuals.pdpo_log_individuals_id;
   
