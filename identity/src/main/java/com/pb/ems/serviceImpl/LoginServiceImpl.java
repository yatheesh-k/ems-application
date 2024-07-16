package com.pb.ems.serviceImpl;

import com.pb.ems.auth.JwtTokenUtil;
import com.pb.ems.common.ResponseBuilder;
import com.pb.ems.exception.ErrorMessageHandler;
import com.pb.ems.exception.IdentityErrorMessageKey;
import com.pb.ems.exception.IdentityException;
import com.pb.ems.model.*;
import com.pb.ems.opensearch.OpenSearchOperations;
import com.pb.ems.persistance.Entity;
import com.pb.ems.service.LoginService;
import com.pb.ems.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private OpenSearchOperations openSearchOperations;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.subject}")
    private String subject;

    @Value("${mail.text}")
    private String text;

    @Value("${mail.subject.otp}")
    private String otpSubject;

    @Value("${mail.text.otp}")
    private String otpText;
    /**
     * @param request
     * @return
     */
    @Override
    public ResponseEntity<?> login(LoginRequest request) throws IdentityException {
        try {
            EmployeeEntity user = openSearchOperations.getEMSAdminById(request.getUsername());
            if (user != null && user.getPassword() != null) {
                String password = new String(Base64.getDecoder().decode(user.getPassword()), StandardCharsets.UTF_8);
                if (request.getPassword().equals(password)) {
                    log.debug("Successfully logged into ems portal for {}", request.getUsername());
                } else {
                    log.error("Invalid credentials");
                    throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                            HttpStatus.FORBIDDEN);
                }
            } else {
                log.error("Invalid credentials");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                        HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("Invalid creds {}", e.getMessage(), e);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                    HttpStatus.FORBIDDEN);
        }
        List<String> roles = new ArrayList<>();
        roles.add(Constants.EMS_ADMIN);
        String token = JwtTokenUtil.generateToken(request.getUsername(), roles);
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(new LoginResponse(token, null)), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> employeeLogin(EmployeeLoginRequest request) throws IdentityException, IOException {
        EmployeeEntity employee;
        Object entity = null;
        try {
            employee = openSearchOperations.getEmployeeById(request.getUsername(), request.getCompany());
            if (employee != null && employee.getPassword() != null) {
                String password = new String(Base64.getDecoder().decode(employee.getPassword()), StandardCharsets.UTF_8);
                if (request.getPassword().equals(password)) {
                    log.debug("Successfully logged into ems portal for {}", request.getUsername());
                } else {
                    log.error("Invalid credentials");
                    throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                            HttpStatus.FORBIDDEN);
                }
            } else {
                log.error("Invalid credentials");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                        HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            log.error("Invalid creds {}", e.getMessage(), e);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                    HttpStatus.FORBIDDEN);
        }
        Long otp = generateOtp();
        sendOtpByEmail(request.getUsername(), otp);
        openSearchOperations.saveOtpToUser(employee, otp,request.getCompany());
        List<String> roles = new ArrayList<>();
        String token= null;
        if (employee != null && employee.getRoles() != null && employee.getRoles().size() > 0) {
            roles.addAll(employee.getRoles());
             token = JwtTokenUtil.generateEmployeeToken(employee.getId(), roles, request.getCompany(),employee.getEmployeeId());
        } else {
            roles.add(Constants.COMPANY_ADMIN);
             token = JwtTokenUtil.generateEmployeeToken(employee.getCompanyId(), roles, request.getCompany(),employee.getEmployeeId());
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(new LoginResponse(token, null)), HttpStatus.OK);
    }

    private void sendOtpByEmail(String emailId, Long otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(emailId);
        mailMessage.setSubject(subject);
        String mailText = text;
        String formattedText = mailText.replace("{emailId}", emailId).replace("{otp}", otp.toString());

        mailMessage.setText(formattedText);
        javaMailSender.send(mailMessage);
        log.info("OTP sent successfully....");//otp is send succesfully...
    }

    private void sendOtpByEmailForPassword(String emailId, Long otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(emailId);
        mailMessage.setSubject(otpSubject);
        String mailText = otpText;
        String formattedText = mailText.replace("{emailId}", emailId).replace("{otp}", otp.toString());

        mailMessage.setText(formattedText);
        javaMailSender.send(mailMessage);
        log.info("OTP sent successfully....");//otp is send succesfully...
    }

    /**
     * @param loginRequest
     * @return
     */
    @Override
    public ResponseEntity<?> logout(OTPRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<?> validateCompanyOtp(OTPRequest request) throws IdentityException {
        EmployeeEntity user;
        try {
            user = openSearchOperations.getEmployeeById(request.getUsername(), request.getCompany());
            if (user == null) {
                log.debug("checking the user details..");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.USER_NOT_FOUND),
                        HttpStatus.NOT_FOUND);
            }
            if (user != null && user.getOtp() != null) {
                Long otp = user.getOtp();
                long currentTime = Instant.now().getEpochSecond();
                log.debug("the user found checking the otp..");

                if (!request.getOtp().equals(otp)) {
                    log.error("Invalid OTP for user.. ");
                    throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_OTP),
                            HttpStatus.FORBIDDEN);
                }

                if (currentTime > user.getExpiryTime()) {
                    log.error("OTP expired for user..." );
                    throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.OTP_EXPIRED),
                            HttpStatus.FORBIDDEN);
                }
                // Clear OTP and expiry time
                user.setOtp(null);
                user.setExpiryTime(null);

                openSearchOperations.updateEmployee(user,request.getCompany());
            } else {
                log.error("Invalid credentials for user..");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                        HttpStatus.FORBIDDEN);
            }
        } catch (IdentityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating OTP for user.." ,e.getMessage(), e);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_CREDENTIALS),
                    HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.OK);
    }

    private Long generateOtp() {
        Random random = new Random();
        Long otp = 100000 + random.nextLong(900000);
        return otp;
    }

    @Override
    public ResponseEntity<?> updateEmsAdmin(LoginRequest request) throws IdentityException {
        try{
            EmployeeEntity user = openSearchOperations.getEMSAdminById(request.getUsername());
            if(user == null ) {
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_USERNAME),
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex ){
            log.error("Exception while fetching user {}, {}", request.getUsername(), ex);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_USERNAME),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String id = Constants.EMS_ADMIN+"_"+request.getUsername();
        Entity entity = EmployeeEntity.builder().
                emailId(request.getUsername()).
                password(request.getPassword()).build();
        openSearchOperations.saveEntity(entity, id, Constants.INDEX_EMS);
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> forgotPassword(EmployeePasswordRequest loginRequest) throws IdentityException {
        EmployeeEntity user ;

        try {
            user = openSearchOperations.getEmployeeById(loginRequest.getUsername(), loginRequest.getCompany());
            if (user == null) {
                log.debug("checking the user details..");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.USER_NOT_FOUND),
                        HttpStatus.NOT_FOUND);
            }


            Long otp = generateOtp();
            sendOtpByEmailForPassword(loginRequest.getUsername(), otp);
            openSearchOperations.saveOtpToUser(user, otp, loginRequest.getCompany());

        } catch (Exception ex) {
            log.error("Exception while fetching user {}, {}", loginRequest.getUsername(), ex);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_USERNAME),
                    HttpStatus.INTERNAL_SERVER_ERROR);


        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updatePasswordForForgot(EmployeePasswordforgot otpRequest) throws IdentityException {
        EmployeeEntity user = null;


        try {
            user = openSearchOperations.getEmployeeById(otpRequest.getUsername(), otpRequest.getCompany());
          CompanyEntity  employee = openSearchOperations.getCompanyById(otpRequest.getCompanyName());
            if (user == null) {
                log.debug("checking the user details..");
                throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.USER_NOT_FOUND),
                        HttpStatus.NOT_FOUND);
            }

            String newPassword = Base64.getEncoder().encodeToString(otpRequest.getPassword().toString().getBytes());

            if (employee !=null){
                employee.setPassword(newPassword);
                openSearchOperations.updateCompany(employee);
            }
            user.setPassword(newPassword);
            openSearchOperations.updateEmployee(user,otpRequest.getCompany());


        } catch (Exception ex) {
            log.error("Exception while fetching user {}, {}", otpRequest.getUsername(), ex);
            throw new IdentityException(ErrorMessageHandler.getMessage(IdentityErrorMessageKey.INVALID_USERNAME),
                    HttpStatus.INTERNAL_SERVER_ERROR);


        }
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(Constants.SUCCESS), HttpStatus.OK);
    }


}
