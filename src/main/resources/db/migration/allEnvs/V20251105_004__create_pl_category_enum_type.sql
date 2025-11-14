/**
* OPAL Program
*
* MODULE      : create_pl_category_enum_type.sql
*
* DESCRIPTION : Create pl_category_enum type for logging categories
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/11/2025    C Cho       1.0         PO-2247 Create pl_category_enum type
**/

CREATE TYPE pl_category_enum AS ENUM ('COLLECTION', 'ALTERATION', 'CONSULTATION', 'DISCLOSURE', 'COMBINATION', 'ERASURE');