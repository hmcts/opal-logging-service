/**
* OPAL Program
*
* MODULE      : alter_pdpo_identifier_type_enum_add_file_handler_int_file.sql
*
* DESCRIPTION : Add FILE_HANDLER_INTERFACE_FILE to t_pdpo_identifier_individual_type_enum ENUM
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------------------------------
* 02/10/2026    TMc            1.0         PO-10845 - Add FILE_HANDLER_INTERFACE_FILE to t_pdpo_identifier_individual_type_enum ENUM
*
**/

ALTER TYPE t_pdpo_identifier_individual_type_enum 
    ADD VALUE IF NOT EXISTS 'FILE_HANDLER_INTERFACE_FILE';
