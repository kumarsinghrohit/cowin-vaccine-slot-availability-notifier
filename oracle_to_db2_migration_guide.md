
# Oracle to Db2 Migration Guide for Maximo

This document provides a detailed workflow for migrating IBM Maximo from Oracle Database to IBM Db2 using a hybrid approach (Oracle Data Pump + Db2 Load + IBM Database Conversion Workbench).

## Migration Flow Diagram

![Migration Diagram](oracle_to_db2_migration_diagram.png)

## Step 1 – Schema Conversion (Oracle → Db2)
- Use IBM Database Conversion Workbench (DCW) to convert Oracle DDL to Db2 DDL.
- Apply schema in Db2:
```bash
db2 -tvf converted_schema.sql
```

## Step 2 – Data Export from Oracle
- Export with Data Pump:
```bash
expdp maximo/password schemas=MAXIMO dumpfile=maximo_data.dmp
```
- Or to CSV:
```sql
sqlplus spool workorder.csv; 
SELECT * FROM workorder;
```

## Step 3 – Data Import into Db2
- Load CSV into Db2:
```bash
db2 LOAD FROM workorder.csv OF DEL INSERT INTO WORKORDER
```
- Handle LOBs:
```bash
db2 LOAD FROM lob_data.del OF DEL MODIFIED BY LOBSINFILE INSERT INTO DOCLINKS
```

## Step 4 – Post Migration Fixes
- Replace Oracle-specific functions:
  - `NVL` → `COALESCE`
  - `SYSDATE` → `CURRENT TIMESTAMP`
  - `TO_CHAR` → `VARCHAR_FORMAT`

## Step 5 – Maximo Configuration
- Update `maximo.properties`:
```properties
mxe.db.driver=com.ibm.db2.jcc.DB2Driver
mxe.db.url=jdbc:db2://<DB2_HOST>:50000/MAXDB
mxe.db.user=maximo
mxe.db.password=xxxxx
```
- Run:
```bash
configdb.bat -action update
```

## Step 6 – Validation
- Compare row counts:
```sql
-- Oracle
SELECT COUNT(*) FROM WORKORDER;

-- Db2
SELECT COUNT(*) FROM WORKORDER;
```
- Run Maximo Integrity Checker
