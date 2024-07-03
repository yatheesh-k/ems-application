package com.pb.employee.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pb.employee.common.ResponseBuilder;
import com.pb.employee.exception.EmployeeErrorMessageKey;
import com.pb.employee.exception.EmployeeException;
import com.pb.employee.exception.ErrorMessageHandler;
import com.pb.employee.opensearch.OpenSearchOperations;
import com.pb.employee.persistance.model.EmployeeEntity;
import com.pb.employee.persistance.model.Entity;
import com.pb.employee.persistance.model.PayslipEntity;
import com.pb.employee.persistance.model.SalaryEntity;
import com.pb.employee.request.PayslipRequest;
import com.pb.employee.request.SalaryRequest;
import com.pb.employee.service.PayslipService;
import com.pb.employee.util.CompanyUtils;
import com.pb.employee.util.Constants;
import com.pb.employee.util.PayslipUtils;
import com.pb.employee.util.ResourceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PayslipServiceImpl implements PayslipService {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OpenSearchOperations openSearchOperations;
    @Autowired
    private EmployeeServiceImpl employeeService;

    @Override
    public ResponseEntity<?> addPaySlip(PayslipRequest payslipRequest, String salaryId, String employeeId) throws EmployeeException, IOException {
        String paySlipId = ResourceIdUtils.generatePayslipId(payslipRequest.getMonth(), payslipRequest.getYear(), employeeId);
        SalaryEntity entity = null;
        Object payslipEntity = null;
        EmployeeEntity employee = null;
        String index = ResourceIdUtils.generateCompanyIndex(payslipRequest.getCompanyName());
            try{
                payslipEntity = openSearchOperations.getById(paySlipId, null, index);
                if(payslipEntity != null) {
                    log.error("employee details existed{}", payslipRequest.getCompanyName());
                    throw new EmployeeException(String.format(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.EMPLOYEE_PAYSLIP_ALREADY_EXISTS),employeeId),
                            HttpStatus.CONFLICT);
                }
            } catch (IOException e) {
                log.error("Unable to get the company details {}", payslipRequest.getCompanyName());
                throw new EmployeeException(String.format(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.INVALID_EMPLOYEE),employeeId),
                        HttpStatus.BAD_REQUEST);
            }
            employee = openSearchOperations.getEmployeeById(employeeId, null, index);
            if(employee ==null){
                log.error("Employee with this {}, is not found", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }


            entity = openSearchOperations.getSalaryById(salaryId, null, index);
            if (entity==null){
                log.error("Exception while fetching employee for salary {}", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_SALARY),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }


            if (!entity.getEmployeeId().equals(employeeId)) {
                log.error("Employee ID mismatch for salary {}: expected {}, found", salaryId, employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.EMPLOYEE_NOT_MATCHING),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

        try{
            Entity payslipProperties = PayslipUtils.maskEmployeePayslipProperties(entity,payslipRequest,paySlipId,salaryId, employeeId);
            Entity result = openSearchOperations.saveEntity(payslipProperties, paySlipId, index);
        } catch (Exception exception) {
            log.error("Unable to save the employee details  {}",exception.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_SAVE_EMPLOYEE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<?> getPayslipById(String companyName, String employeeId, String payslipId) throws EmployeeException, IOException {
        String index = ResourceIdUtils.generateCompanyIndex(companyName);
        EmployeeEntity employee = null;
        PayslipEntity entity = null;
        try {
           employee = openSearchOperations.getEmployeeById(employeeId, null, index);
           if (employee == null){
               log.error("Employee with this {}, is not found", employeeId);
               throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                       HttpStatus.INTERNAL_SERVER_ERROR);
           }

           entity=openSearchOperations.getPayslipById(payslipId, null, index);
           if (entity==null){
               log.error("Employee with this payslip {}, is not found", payslipId);
               throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_PAYSLIP),
                       HttpStatus.INTERNAL_SERVER_ERROR);
           }
           if (!entity.getEmployeeId().equals(employeeId)){
               log.error("Employee ID mismatch for payslipId {}: expected {}, found {}", payslipId, employeeId, entity.getEmployeeId());
               throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.EMPLOYEE_NOT_MATCHING),
                       HttpStatus.INTERNAL_SERVER_ERROR);
           }
        }
        catch (Exception ex) {
            log.error("Exception while fetching salaries for employees {}: {}", employeeId, ex.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(entity), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> getEmployeePayslips(String companyName, String employeeId) throws EmployeeException {
        String index = ResourceIdUtils.generateCompanyIndex(companyName);
        EmployeeEntity employee = null;
        List<PayslipEntity> allPayslips =null;
        try {
            // Fetch employee details
            employee = openSearchOperations.getEmployeeById(employeeId, null, index);
            if (employee == null) {
                log.error("Employee with ID {} is not found", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.NOT_FOUND);
            }

            allPayslips = openSearchOperations.getEmployeePayslip(companyName, employeeId);

            if (allPayslips.isEmpty()) {
                log.warn("No matching payslips found for employee with ID {}", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.EMPLOYEE_NOT_MATCHING),
                        HttpStatus.NOT_FOUND);
            }

        } catch (Exception ex) {
            log.error("Exception while fetching payslips for employee {}: {}", employeeId, ex.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_PAYSLIP),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(allPayslips), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> deleteEmployeePayslipById(String companyName, String employeeId,String payslipId) throws EmployeeException{
        String index = ResourceIdUtils.generateCompanyIndex(companyName);
        EmployeeEntity employee = null;
        Object entity = null;
        try {
            employee = openSearchOperations.getEmployeeById(employeeId, null, index);
            if (employee == null){
                log.error("Employee with this {}, is not found", employeeId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            entity=openSearchOperations.getById(payslipId, null, index);
            if (entity==null){
                log.error("Employee with this payslip {}, is not found", payslipId);
                throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_PAYSLIP),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            openSearchOperations.deleteEntity(payslipId, index);
        }
        catch (Exception ex) {
            log.error("Exception while deleting salaries for employees {}: {}", payslipId, ex.getMessage());
            throw new EmployeeException(ErrorMessageHandler.getMessage(EmployeeErrorMessageKey.UNABLE_GET_EMPLOYEES_SALARY),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.DELETED), HttpStatus.OK);

    }

}
