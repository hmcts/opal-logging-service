/**
* OPAL Program
*
* MODULE      : create_pdpo_log_table.sql
*
* DESCRIPTION : Create pdpo_log table in the new Logging service database
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/11/2025    C Cho       1.0         PO-2247 Create pdpo_log table in the new Logging service database
*
**/

CREATE TABLE pdpo_log (
    pdpo_log_id                 BIGINT      NOT NULL,
    created_by_identifier       VARCHAR(50) NOT NULL,
    created_by_identifier_type  VARCHAR(30) NOT NULL,
    created_at                  TIMESTAMP   NOT NULL,
    ip_address                  INET        NOT NULL,
    category pl_category_enum   NOT NULL,
    recipient_identifier        VARCHAR(50),
    recipient_identifier_type   VARCHAR(30),
    pdpo_identifiers_id         BIGINT      NOT NULL,
    
    CONSTRAINT pdpo_log_pk PRIMARY KEY (pdpo_log_id),
    CONSTRAINT pdpo_log_identifiers_fk FOREIGN KEY (pdpo_identifiers_id) REFERENCES pdpo_identifiers(pdpo_identifiers_id)
);

COMMENT ON COLUMN pdpo_log.pdpo_log_id IS 'Generated primary key for the access log';
COMMENT ON COLUMN pdpo_log.created_by_identifier IS 'The identifier of the user/system that this log relates too. e.g. user_id or a system name';
COMMENT ON COLUMN pdpo_log.created_by_identifier_type IS 'The type of data the created_by_identifier relates to. Examples include OPAL_USER_ID EXTERNAL_SERVICE';
COMMENT ON COLUMN pdpo_log.created_at IS 'The time this log event was captured. This comes from the inbound JSON and not when this data was saved';
COMMENT ON COLUMN pdpo_log.ip_address IS 'The IP address of the user who performed the PDPO';
COMMENT ON COLUMN pdpo_log.category IS 'The category of the log. Examples include COLLECTION ALTERATION CONSULTATION DISCLOSURE (including Transfers) COMBINATION ERASURE';
COMMENT ON COLUMN pdpo_log.recipient_identifier IS 'The recipient identifier of who is getting the protected information Only recorded where the PDPO Category is Disclosure';
COMMENT ON COLUMN pdpo_log.recipient_identifier_type IS 'The identifier type of the recipient Examples include DEFENDANT MINOR_CREDITOR OPAL_USER EXTERNAL_SYSTEM Only recorded where the PDPO Category is Disclosure';
COMMENT ON COLUMN pdpo_log.pdpo_identifiers_id IS 'The identifier id from the pdpo_identifiers table';