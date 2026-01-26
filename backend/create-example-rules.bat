@echo off
REM Script to create example login rules for all four types
REM Make sure backend is running on http://localhost:8080

echo Creating example login rules...
echo.

REM Note: ALL_DAYS "Unrestricted Access" already exists as default rule

REM 2. ALL_DAYS_WITH_TIME - Business Hours (9 AM to 5 PM every day)
echo Creating ALL_DAYS_WITH_TIME rule: Business Hours...
curl -X POST http://localhost:8080/api/admin/login-rules ^
  -H "Content-Type: application/json" ^
  -d "{\"ruleName\":\"Business Hours\",\"ruleType\":\"ALL_DAYS_WITH_TIME\",\"description\":\"Standard 9 AM to 5 PM working hours, Monday through Sunday\",\"schedules\":[{\"dayOfWeek\":\"ALL\",\"startTime\":\"09:00\",\"endTime\":\"17:00\",\"isActive\":true}]}"
echo.
echo.

REM 3. DAY_ANY_TIME - Weekdays Only (Monday-Friday, any time)
echo Creating DAY_ANY_TIME rule: Weekdays Only...
curl -X POST http://localhost:8080/api/admin/login-rules ^
  -H "Content-Type: application/json" ^
  -d "{\"ruleName\":\"Weekdays Only\",\"ruleType\":\"DAY_ANY_TIME\",\"description\":\"Tracking allowed any time on weekdays only\",\"schedules\":[{\"dayOfWeek\":\"MONDAY\",\"startTime\":null,\"endTime\":null,\"isActive\":true},{\"dayOfWeek\":\"TUESDAY\",\"startTime\":null,\"endTime\":null,\"isActive\":true},{\"dayOfWeek\":\"WEDNESDAY\",\"startTime\":null,\"endTime\":null,\"isActive\":true},{\"dayOfWeek\":\"THURSDAY\",\"startTime\":null,\"endTime\":null,\"isActive\":true},{\"dayOfWeek\":\"FRIDAY\",\"startTime\":null,\"endTime\":null,\"isActive\":true}]}"
echo.
echo.

REM 4. CUSTOM - Flexible Schedule (Different times for different days)
echo Creating CUSTOM rule: Flexible Schedule...
curl -X POST http://localhost:8080/api/admin/login-rules ^
  -H "Content-Type: application/json" ^
  -d "{\"ruleName\":\"Flexible Schedule\",\"ruleType\":\"CUSTOM\",\"description\":\"Different working hours for different days of the week\",\"schedules\":[{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"08:00\",\"endTime\":\"16:00\",\"isActive\":true},{\"dayOfWeek\":\"TUESDAY\",\"startTime\":\"08:00\",\"endTime\":\"16:00\",\"isActive\":true},{\"dayOfWeek\":\"WEDNESDAY\",\"startTime\":\"10:00\",\"endTime\":\"18:00\",\"isActive\":true},{\"dayOfWeek\":\"THURSDAY\",\"startTime\":\"10:00\",\"endTime\":\"18:00\",\"isActive\":true},{\"dayOfWeek\":\"FRIDAY\",\"startTime\":\"09:00\",\"endTime\":\"15:00\",\"isActive\":true}]}"
echo.
echo.

REM 5. CUSTOM - Night Shift (Example with overnight shift split)
echo Creating CUSTOM rule: Night Shift...
curl -X POST http://localhost:8080/api/admin/login-rules ^
  -H "Content-Type: application/json" ^
  -d "{\"ruleName\":\"Night Shift\",\"ruleType\":\"CUSTOM\",\"description\":\"Night shift workers - demonstrates overnight shift handling\",\"schedules\":[{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"22:00\",\"endTime\":\"23:59\",\"isActive\":true},{\"dayOfWeek\":\"TUESDAY\",\"startTime\":\"00:00\",\"endTime\":\"06:00\",\"isActive\":true},{\"dayOfWeek\":\"TUESDAY\",\"startTime\":\"22:00\",\"endTime\":\"23:59\",\"isActive\":true},{\"dayOfWeek\":\"WEDNESDAY\",\"startTime\":\"00:00\",\"endTime\":\"06:00\",\"isActive\":true},{\"dayOfWeek\":\"WEDNESDAY\",\"startTime\":\"22:00\",\"endTime\":\"23:59\",\"isActive\":true},{\"dayOfWeek\":\"THURSDAY\",\"startTime\":\"00:00\",\"endTime\":\"06:00\",\"isActive\":true},{\"dayOfWeek\":\"THURSDAY\",\"startTime\":\"22:00\",\"endTime\":\"23:59\",\"isActive\":true},{\"dayOfWeek\":\"FRIDAY\",\"startTime\":\"00:00\",\"endTime\":\"06:00\",\"isActive\":true}]}"
echo.
echo.

echo All example login rules created successfully!
echo.
echo You can now view them at: http://localhost:3000/admin/login-rules
echo Or test them by assigning to employees in: http://localhost:3000/employees
pause
