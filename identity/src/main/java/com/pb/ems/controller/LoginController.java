package com.pb.ems.controller;

import com.pb.ems.auth.JwtTokenUtil;
import com.pb.ems.common.ResponseBuilder;
import com.pb.ems.common.ResponseObject;
import com.pb.ems.config.SwaggerConfig;
import com.pb.ems.exception.IdentityException;
import com.pb.ems.model.*;
import com.pb.ems.service.LoginService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Slf4j
@CrossOrigin(origins="*")
@RequestMapping("/")
@io.swagger.v3.oas.annotations.tags.Tag(name = SwaggerConfig.EMPLOYEE_TAG)
@io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)),responseCode = "400", description = "The request is malformed or invalid."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)),responseCode = "403", description= "The user does not have the necessary privileges to perform the operation."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)),responseCode = "500", description = "An internal server error occurred."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)),responseCode = "503", description = "A service is unreachable."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)),responseCode = "504", description = "Gateway Timeout Error" ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseObject.class)))
})
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("emsadmin/login")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "${api.login.tag}", description = "{api.login.description}")
    @ResponseStatus(HttpStatus.OK)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description= "OK")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) throws IdentityException {
        return loginService.login(request);
    }

    @PatchMapping("emsadmin/login")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "${api.login.tag}", description = "${api.login.description}")
    @ResponseStatus(HttpStatus.OK)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description= "OK")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid LoginRequest request) throws IdentityException {
        return loginService.updateEmsAdmin(request);
    }

    @PostMapping("/token/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "${api.tokenvalidate.tag}", description = "${api.tokenvalidate.description}")
    @ResponseStatus(HttpStatus.OK)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description= "OK")
    public ResponseEntity<?> validateToken(@RequestBody @Valid ValidateLoginRequest request) throws IdentityException {
        return new ResponseEntity<>(
                ResponseBuilder.builder().build().createSuccessResponse(JwtTokenUtil.validateToken(request.getToken())), HttpStatus.OK);
    }

    @PostMapping("company/login")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "${api.login.tag}", description = "${api.login.description}")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description= "CREATED")
    public ResponseEntity<?> compayLogin(@RequestBody @Valid EmployeeLoginRequest request) throws IdentityException {
        return loginService.employeeLogin(request);
    }

    @PostMapping("validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "${api.login.otp}", description = "${api.adminlogin.description}")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description= "CREATED")
    public ResponseEntity<?> validateCompanyOtp(@RequestBody @Valid OTPRequest request) throws IdentityException {
        return loginService.validateCompanyOtp(request);
    }


}
