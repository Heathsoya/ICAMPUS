-- Optional CSV import helper.
-- This file is not required for application startup.
--
-- Example:
-- mysql --local-infile=1 -u root -p icampus
-- SOURCE backend/db/import_knowledge_base_csv.sql;

LOAD DATA LOCAL INFILE 'backend/db/csv/knowledge_base_sample.csv'
INTO TABLE knowledge_base
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(question, answer, category, keywords, source)
SET updated_at = CURRENT_TIMESTAMP;
