package com.pbt.ems.repository;

import com.pbt.ems.entity.PaySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaySlipRepository extends JpaRepository<PaySlip, Long> {

    Optional<PaySlip> findByEmployeeIdAndMonthAndYear(String employeeId, String month, Long year);

    List<PaySlip> findByEmployeeId(String employeeId);
}
