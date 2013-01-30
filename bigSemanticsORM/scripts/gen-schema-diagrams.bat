@echo off

set SCHEMA_SPY_PATH=C:\libs\schemaSpy_5.0.0.jar
set HOST=localhost
set DB=mmd_orm_test
set DRIVER_PATH=C:\libs\postgresql-9.0-801.jdbc4.jar
set USER=quyin
set PASS=quyindbpwd
set OUTPUT_DIR=C:\tmp\db-schemas\mmd_orm_test

rmdir /s %OUTPUT_DIR%
mkdir %OUTPUT_DIR%
java -jar %SCHEMA_SPY_PATH% -t pgsql -host %HOST% -db %DB% -s public -dp %DRIVER_PATH% -u %USER% -p %PASS% -o %OUTPUT_DIR%
