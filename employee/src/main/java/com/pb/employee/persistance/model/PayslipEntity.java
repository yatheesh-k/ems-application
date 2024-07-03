package com.pb.employee.persistance.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayslipEntity implements Entity  {

    private String payslipId;
    private String employeeId;
    private  String salaryId;
    private SalaryEntity salary;
    private String month;
    private String year;

    private String type;
}
