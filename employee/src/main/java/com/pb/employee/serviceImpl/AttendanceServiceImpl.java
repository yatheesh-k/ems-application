
package com.pb.employee.serviceImpl;


import com.pb.employee.common.ResponseBuilder;
import com.pb.employee.common.ResponseObject;
import com.pb.employee.exception.EmployeeErrorMessageKey;
import com.pb.employee.exception.EmployeeException;
import com.pb.employee.exception.ErrorMessageHandler;
import com.pb.employee.opensearch.OpenSearchOperations;
import com.pb.employee.persistance.model.*;
import com.pb.employee.request.AttendanceUpdateRequest;
import com.pb.employee.request.EmployeeStatus;
import org.apache.poi.ss.usermodel.*;
import com.pb.employee.request.AttendanceRequest;
import com.pb.employee.service.AttendanceService;
import com.pb.employee.util.CompanyUtils;
import com.pb.employee.util.Constants;
import com.pb.employee.util.ResourceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;

@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private OpenSearchOperations openSearchOperations;


    private static final Map<String, Month> MONTH_NAME_MAP = createMonthNameMap();
    private static Map<String, Month> createMonthNameMap() {
        Map<String, Month> map = new HashMap<>();
        for (Month month : Month.values()) {
            map.put(month.name().substring(0, 1) + month.name().substring(1).toLowerCase(), month);
        }
        return map;
    }

    @Override
    public ResponseEntity<?> uploadAttendanceFile(String company, MultipartFile file) throws EmployeeException {
        if (file.isEmpty()) {
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.EMPTY_FILE), HttpStatus.BAD_REQUEST);
        }
        List<AttendanceRequest> attendanceRequests;

        try {
            attendanceRequests = parseExcelFile(file, company);
            if (attendanceRequests.isEmpty()) {
                return new ResponseEntity<>(
                        ResponseBuilder.builder().build().
                                createFailureResponse(new Exception(String.valueOf(ErrorMessageHandler
                                        .getMessage(EmployeeErrorMessageKey.EMPTY_FILE)))),
                        HttpStatus.NOT_FOUND);
            }

            // Validate all attendance requests before adding
            for (AttendanceRequest attendanceRequest : attendanceRequests) {
                String index = ResourceIdUtils.generateCompanyIndex(attendanceRequest.getCompany());

                // Validate Employee using employeeId or emailId
                String employeeId = ResourceIdUtils.generateEmployeeResourceId(attendanceRequest.getEmailId());
                EmployeeEntity employee = openSearchOperations.getEmployeeById(employeeId, null, index);

                // If employee is not found, throw an error
                if (employee == null) {
                    log.error("Employee not found for attendance and ID: {}", employeeId);
                    return new ResponseEntity<>(
                            ResponseBuilder.builder().build().
                                    createFailureResponse(new Exception(String.valueOf(ErrorMessageHandler
                                            .getMessage(EmployeeErrorMessageKey.EMPLOYEE_NOT_FOUND)))),
                            HttpStatus.NOT_FOUND);
                }
                String requestEmployeeId = attendanceRequest.getEmployeeId();
                log.debug("Received employee ID as a string: " + requestEmployeeId);

                // Validate firstName, lastName, and emailId
                if (!attendanceRequest.getFirstName().equalsIgnoreCase(employee.getFirstName()) ||
                        !attendanceRequest.getLastName().equalsIgnoreCase(employee.getLastName()) ||
                        !requestEmployeeId.equalsIgnoreCase(employee.getEmployeeId())) {
                    log.error("Validation failed for employee ID: {}. Details provided do not match.", employeeId);
                    return new ResponseEntity<>(
                            ResponseBuilder.builder().build().
                                    createFailureResponse(new Exception(String.valueOf(ErrorMessageHandler
                                            .getMessage(EmployeeErrorMessageKey.INVALID_EMPLOYEE_DETAILS)))),
                            HttpStatus.BAD_REQUEST);
                }

                if (EmployeeStatus.INACTIVE.getStatus().equals(employee.getStatus())) {
                    log.error("The employee is inactive {}", employeeId);
                    return new ResponseEntity<>(
                            ResponseBuilder.builder().build().
                                    createFailureResponse(new Exception(String.valueOf(ErrorMessageHandler
                                            .getMessage(EmployeeErrorMessageKey.EMPLOYEE_INACTIVE)))),
                            HttpStatus.CONFLICT);
                }
            }
            // If all validations pass, add attendance
            for (AttendanceRequest attendanceRequest : attendanceRequests) {
                addAttendanceOfEmployees(attendanceRequest);
            }

        } catch (Exception e) {
            log.error("Error processing the uploaded file: {}", e.getMessage(), e);
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.FAILED_TO_PROCESS), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.debug("The attendance added successfully...");
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getAllEmployeeAttendance(String companyName, String employeeId, String month, String year) throws EmployeeException {
        List<AttendanceEntity> attendanceEntities = null;
        String index = ResourceIdUtils.generateCompanyIndex(companyName);

        try {
            // Fetch the actual employeeId from the database based on the generatedEmployeeId
            EmployeeEntity employee = openSearchOperations.getEmployeeById(employeeId,null,index);

            // Check if employee exists
            if (employee == null) {
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.NOT_FOUND);
            }
            // Call the method to get attendance based on whether the month is provided or not
            if ((month != null && !month.isEmpty())||(year!= null && !year.isEmpty())) {
                attendanceEntities = openSearchOperations.getAttendanceByMonthAndYear(companyName,employeeId, month, year);
            }
            // Unmask sensitive properties if required
            for (AttendanceEntity attendanceEntity : attendanceEntities) {
                CompanyUtils.unMaskAttendanceProperties(attendanceEntity);
            }
            // Return success response with the retrieved attendance records
            return new ResponseEntity<>(ResponseBuilder.builder().build().createSuccessResponse(attendanceEntities), HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Exception while fetching attendance for employees {}: {}", companyName, ex.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_TO_GET_ATTENDANCE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteEmployeeAttendanceById(String companyName, String employeeId, String attendanceId) throws EmployeeException {
        String index = ResourceIdUtils.generateCompanyIndex(companyName);
        AttendanceEntity entity = null;
        try {
            entity = openSearchOperations.getAttendanceById(attendanceId, null, index);
            if (entity==null){
                log.error("Exception while fetching employee for the attendance {}", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!entity.getEmployeeId().equals(employeeId)) {
                log.error("Employee ID mismatch for attendance {}: expected {}, found", attendanceId, employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            openSearchOperations.deleteEntity(attendanceId, index);
        }
        catch (Exception ex) {
            log.error("Exception while deleting attendance for employees {}: {}", attendanceId, ex.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_ATTENDANCE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.DELETED), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> updateEmployeeAttendanceById(String company, String employeeId, String attendanceId, AttendanceUpdateRequest updateRequest) throws EmployeeException{
        String index = ResourceIdUtils.generateCompanyIndex(company);
        AttendanceEntity entity = null;
        try {
            entity = openSearchOperations.getAttendanceById(attendanceId, null, index);
            if (entity==null){
                log.error("Exception while fetching employee for attendance {}", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_ATTENDANCE),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!entity.getEmployeeId().equals(employeeId)) {
                log.error("Employee ID mismatch for attendance details {}: expected {}, found", attendanceId, employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("Exception while fetching user {}:", employeeId, ex);
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_ATTENDANCE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Entity employeeAttendance = CompanyUtils.maskAttendanceUpdateProperties(updateRequest, entity);
        openSearchOperations.saveEntity(employeeAttendance, attendanceId, index);
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.OK);
    }
    private List<AttendanceRequest> parseExcelFile(MultipartFile file, String company) throws Exception {
        List<AttendanceRequest> attendanceRequests = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
            try (InputStream excelIs = file.getInputStream()) {
                log.info("Successfully opened input stream for file: {}", fileName);
                Workbook wb = null;
                try {
                    wb = new XSSFWorkbook(excelIs);
                } catch (Exception e) {
                    log.error("Failed to create workbook for file: {}", e.getMessage(), e);
                    throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.FAILED_TO_CREATE), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Sheet sheet = wb.getSheetAt(0);
                log.info("Successfully retrieved sheet: '{}', with {} rows.", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());
                Iterator<Row> rowIt = sheet.rowIterator();
                if (!rowIt.hasNext()) {
                    log.warn("No rows found in the sheet '{}'", sheet.getSheetName());
                }
                // Get current year and month
                LocalDate now = LocalDate.now();
                String currentYear = String.valueOf(now.getYear());
                String currentMonth = now.getMonth().name().substring(0, 1) + now.getMonth().name().substring(1).toLowerCase(); // Capitalize only first letter
                while (rowIt.hasNext()) {
                    Row currentRow = rowIt.next();
                    log.info("Processing row number: {}", currentRow.getRowNum());
                    if (currentRow.getRowNum() == 0) {
                        log.info("Skipping header row.");
                        continue;
                    }
                    if (StringUtils.isEmptyOrWhitespace(getCellValue(currentRow.getCell(0)))
                            || StringUtils.isEmptyOrWhitespace(getCellValue(currentRow.getCell(1)))
                            || StringUtils.isEmptyOrWhitespace(getCellValue(currentRow.getCell(2)))
                            || StringUtils.isEmptyOrWhitespace(getCellValue(currentRow.getCell(3)))
                            || StringUtils.isEmptyOrWhitespace(getCellValue(currentRow.getCell(4)))) {
                        log.warn("Skipping row {} as one or more critical cells are empty", currentRow.getRowNum());
                        continue;
                    }
                    AttendanceRequest attendanceRequest = new AttendanceRequest();
                    attendanceRequest.setCompany(company);
                    attendanceRequest.setEmployeeId(getCellValue(currentRow.getCell(0)));
                    attendanceRequest.setFirstName(getCellValue(currentRow.getCell(1)));
                    attendanceRequest.setLastName(getCellValue(currentRow.getCell(2)));
                    attendanceRequest.setEmailId(getCellValue(currentRow.getCell(3)));
                    // Set default year and month
                    attendanceRequest.setYear(currentYear);
                    attendanceRequest.setMonth(currentMonth);
                    // Determine the number of days in the given month and year
                    int year = Integer.parseInt(currentYear);
                    Month month = MONTH_NAME_MAP.get(currentMonth); // Use the map to get Month object
                    if (month == null) {
                        log.error("Invalid month name provided: {}", currentMonth);
                        throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.INVALID_MONTH_NAME), HttpStatus.BAD_REQUEST);
                    }
                    YearMonth yearMonth = YearMonth.of(year, month);
                    int totalDaysInMonth = yearMonth.lengthOfMonth(); // Total days in the month
                    // Validate noOfWorkingDays
                    String noOfWorkingDaysStr = getCellValue(currentRow.getCell(4));
                    int noOfWorkingDays;
                    try {
                        // Convert to Double first, then to Integer
                        double noOfWorkingDaysDouble = Double.parseDouble(noOfWorkingDaysStr);
                        noOfWorkingDays = (int) Math.round(noOfWorkingDaysDouble); // Use rounding to handle fractional values
                        // Check if noOfWorkingDays is less than or equal to totalDaysInMonth
                        if (noOfWorkingDays > totalDaysInMonth) {
                            log.error("No of working days '{}' exceeds total days in the month '{}'", noOfWorkingDays, totalDaysInMonth);
                            throw new EmployeeException(ErrorMessageHandler
                                    .getMessage(EmployeeErrorMessageKey.INVALID_NO_OF_WORKING_DAYS),
                                    HttpStatus.BAD_REQUEST);
                        }
                        // Set totalWorkingDays as total days in month since it is not provided
                        attendanceRequest.setTotalWorkingDays(String.valueOf(totalDaysInMonth));
                        attendanceRequest.setNoOfWorkingDays(String.valueOf(noOfWorkingDays));
                    } catch (NumberFormatException e) {
                        log.error("Error parsing numeric values for working days: {}", e.getMessage());
                        throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.NUMBER_EXCEPTION), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    attendanceRequests.add(attendanceRequest);
                    log.info("Added attendance request for employee ID: {}", attendanceRequest.getEmployeeId());
                }
                wb.close();
            } catch (IOException e) {
                log.error("Failed to process file: {}", e.getMessage(), e);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.FAILED_TO_PROCESS), HttpStatus.CONFLICT);
            }
        } else {
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.INVALID_FORMAT), HttpStatus.BAD_REQUEST);
        }
        return attendanceRequests;
    }


    public String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Format numeric values to remove decimal points if they are integers
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (int) numericValue) {
                        return String.valueOf((int) numericValue); // Remove decimal point for whole numbers
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            default:
                return "";
        }
    }
    private ResponseEntity<?> addAttendanceOfEmployees(AttendanceRequest attendanceRequest) throws EmployeeException, IOException {
        log.debug("Attendance adding method is started ..");
        String index = ResourceIdUtils.generateCompanyIndex(attendanceRequest.getCompany());
        String employeeId = ResourceIdUtils.generateEmployeeResourceId(attendanceRequest.getEmailId());
        EmployeeEntity employee = null;

        try {
            // Fetch the employee details
            employee = openSearchOperations.getEmployeeById(employeeId, null, index);

            if (employee == null) {
                log.error("Employee not found for ID: {}", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.NOT_FOUND);
            }
            // Generate attendance ID and check if it already exists
            String attendanceId = ResourceIdUtils.generateAttendanceId(attendanceRequest.getCompany(), employee.getId(), attendanceRequest.getYear(), attendanceRequest.getMonth());
            Object object = openSearchOperations.getById(attendanceId, null, index);

            if (object != null) {
                log.error("Attendance ID already exists {}", attendanceId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.ATTENDANCE_ALREADY_EXISTS),
                        HttpStatus.NOT_ACCEPTABLE);
            }
            // Create and save the new AttendanceEntity
            Entity attendanceEntity = CompanyUtils.maskAttendanceProperties(attendanceRequest, attendanceId, employeeId);
            openSearchOperations.saveEntity(attendanceEntity, attendanceId, index);

        } catch (Exception exception) {
            log.error("Unable to save the employee attendance details {} {}", attendanceRequest.getType(), exception.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_TO_SAVE_ATTENDANCE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.CREATED);
    }

}