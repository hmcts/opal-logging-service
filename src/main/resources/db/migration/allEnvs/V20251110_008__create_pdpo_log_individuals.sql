/**
* CGI OPAL Program
*
* MODULE      : create_pdpo_log_individuals.sql
*
* DESCRIPTION : Creates the PDPO_LOG_INDIVIDUALS table in the Logging service database
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 07/11/2025    CL          1.0         PO-2270 - Creates the PDPO_LOG_INDIVIDUALS table in the Logging service database
*
**/
CREATE TABLE pdpo_log_individuals (
pdpo_log_individuals_id BIGINT      NOT NULL,
individual_identifier   VARCHAR(50) NOT NULL,
individual_type         VARCHAR(30) NOT NULL,
pdpo_log_id             BIGINT      NOT NULL,
CONSTRAINT pdpo_log_individuals_pk  PRIMARY KEY 
 (
  pdpo_log_individuals_id
 )  
);

ALTER TABLE pdpo_log_individuals
ADD CONSTRAINT pli_pdpo_log_id_fk FOREIGN KEY
(
  pdpo_log_id 
)
REFERENCES pdpo_log
(
  pdpo_log_id 
);

COMMENT ON COLUMN pdpo_log_individuals.pdpo_log_individuals_id IS 'Primary key created from a sequence';
COMMENT ON COLUMN pdpo_log_individuals.individual_identifier IS 'The identifier of the individual whose data was subject to a PDPO';
COMMENT ON COLUMN pdpo_log_individuals.individual_type IS 'The identifier type of the individual whose data was subject to a PDPO';
COMMENT ON COLUMN pdpo_log_individuals.pdpo_log_id IS 'The pdpo_log row that this individual relates to';
