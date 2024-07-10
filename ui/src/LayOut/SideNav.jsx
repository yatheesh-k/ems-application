  import { jwtDecode } from "jwt-decode";
  import React, { useEffect, useState } from "react";
  import {
    Diagram3Fill,
    EraserFill,
    FileMedicalFill,
    PersonVcardFill,
    Receipt,
    Speedometer2,
  } from "react-bootstrap-icons";
  import { Link, useLocation } from "react-router-dom";

  const SideNav = () => {
    const [isPayrollOpen, setIsPayrollOpen] = useState(false); // State for managing PayRoll dropdown
    const [isAttendanceOpen, setIsAttendanceOpen] = useState(false); // State for managing Attendance dropdown
    const [isCompanyOpen, setIsCompanyOpen] = useState(false); // State for managing Company dropdown
    const [roles, setRoles] = useState([]);

    const location = useLocation();
    const userImageBase64 = sessionStorage.getItem("imageFile"); // Assuming the base64 image is stored in sessionStorage

    const token = sessionStorage.getItem("token");

    useEffect(() => {
      if (token) {
        const decodedToken = jwtDecode(token);
        setRoles(decodedToken.roles || []);
      }
    }, [token]);
  
  useEffect(() => {
    if (
      location.pathname === "/companyRegistration" ||
      location.pathname.startsWith("/companyView")
    ) {
      setIsCompanyOpen(true);
    } else {
      setIsCompanyOpen(false);
    }
  }, [location]);


  useEffect(() => {
    if (
      location.pathname === "/companySalaryStructure" ||
      location.pathname === "/employeeSalaryStructure" ||
      location.pathname === "/employeeSalaryList" ||
      location.pathname === "/payslipGeneration" ||
      location.pathname === "/payslipsList"||
      location.pathname === "/increment" ||
      location.pathname === "/incrementList"
    ) {
      setIsPayrollOpen(true);
    } else {
      setIsPayrollOpen(false);
    }
  }, [location]);

  useEffect(() => {
    if (
      location.pathname === "/addAttendance" ||
      location.pathname === "/attendanceList" ||
      location.pathname === "/attendanceReport"
    ) {
      setIsAttendanceOpen(true);
    } else {
      setIsAttendanceOpen(false);
    }
  }, [location]);

  
  useEffect(() => {
    if (
      location.pathname === "/companyRegistration" ||
      location.pathname.startsWith("/companyView")
    ) {
      setIsCompanyOpen(true);
    } else {
      setIsCompanyOpen(false);
    }
  }, [location]);

  useEffect(() => {
    if (
      location.pathname === "/companySalaryStructure" ||
      location.pathname === "/employeeSalaryStructure" ||
      location.pathname === "/employeeSalaryList" ||
      location.pathname === "/payslipGeneration" ||
      location.pathname === "/payslipsList"||
      location.pathname === "/increment" ||
      location.pathname === "/incrementList"
    ) {
      setIsPayrollOpen(true);
    } else {
      setIsPayrollOpen(false);
    }
  }, [location]);

  useEffect(() => {
    if (
      location.pathname === "/addAttendance" ||
      location.pathname === "/attendanceList" ||
      location.pathname === "/attendanceReport"
    ) {
      setIsAttendanceOpen(true);
    } else {
      setIsAttendanceOpen(false);
    }
  }, [location]);

    useEffect(() => {
      if (
        location.pathname === "/companyRegistration" ||
        location.pathname.startsWith("/companyView")
      ) {
        setIsCompanyOpen(true);
      } else {
        setIsCompanyOpen(false);
      }
    }, [location]);

    useEffect(() => {
      if (
        location.pathname === "/companySalaryStructure" ||
        location.pathname === "/employeeSalaryStructure" ||
        location.pathname === "/employeeSalaryList" ||
        location.pathname === "/payslipGeneration" ||
        location.pathname === "/payslipsList"||
        location.pathname === "/increment" ||
        location.pathname === "/incrementList" 
      ) {
        setIsPayrollOpen(true);
      } else {
        setIsPayrollOpen(false);
      }
    }, [location]);

    useEffect(() => {
      if (
        location.pathname === "/addAttendance" ||
        location.pathname === "/attendanceList" ||
        location.pathname === "/attendanceReport"
      ) {
        setIsAttendanceOpen(true);
      } else {
        setIsAttendanceOpen(false);
      }
    }, [location]);
    console.log(roles);

    useEffect(() => {
      if (
        location.pathname === "/companyRegistration" ||
        location.pathname.startsWith("/companyView")
      ) {
        setIsCompanyOpen(true);
      } else {
        setIsCompanyOpen(false);
      }
    }, [location]);

    useEffect(() => {
      if (
        location.pathname === "/companySalaryStructure" ||
        location.pathname === "/employeeSalaryStructure" ||
        location.pathname === "/employeeSalaryList" ||
        location.pathname === "/payslipGeneration" ||
        location.pathname === "/payslipsList"||
        location.pathname === "/increment" ||
        location.pathname === "/incrementList" 
      ) {
        setIsPayrollOpen(true);
      } else {
        setIsPayrollOpen(false);
      }
    }, [location]);

    useEffect(() => {
      if (
        location.pathname === "/addAttendance" ||
        location.pathname === "/attendanceList" ||
        location.pathname === "/attendanceReport"
      ) {
        setIsAttendanceOpen(true);
      } else {
        setIsAttendanceOpen(false);
      }
    }, [location]);

    const togglePayroll = (e) => {
      e.preventDefault(); // Prevent default anchor behavior
      setIsPayrollOpen(!isPayrollOpen);
      setIsCompanyOpen(false);
      setIsAttendanceOpen(false);
    };

    const toggleCompany = (e) => {
      e.preventDefault();
      setIsCompanyOpen(!isCompanyOpen);
      setIsAttendanceOpen(false);
      setIsPayrollOpen(false);
    };

    const toggleAttendance = (e) => {
      e.preventDefault(); // Prevent default anchor behavior
      setIsAttendanceOpen(!isAttendanceOpen);
      setIsCompanyOpen(false);
      setIsPayrollOpen(false);
    };

    const getImageSrc = () => {
      if (roles === "ems_admin") {
        return "assets/img/pathbreaker_logo.png";
      }
      return userImageBase64
        ? `data:image/png;base64,${userImageBase64}`
        : "assets/img/pathbreaker_logo.png"; // Fallback to a default image if base64 is not available
    };
    
    return (
      <nav id="sidebar" class="sidebar js-sidebar">
        <div className="sidebar-content js-simplebar">
          <a className="sidebar-brand" href="/main">
            <span>
              <img
                className="align-middle"
                src={getImageSrc()}
                alt="Logo"
                style={{ height: "80px", width: "180px" }}
              />
            </span>
          </a>
          <ul className="sidebar-nav mt-2">
            {roles.includes("ems_admin") && (
              <>
                <li
                  className={`sidebar-item ${
                    location.pathname === "/main" ? "active" : ""
                  }`}
                >
                  <Link className="sidebar-link" to={"/main"}>
                    <i
                      class="bi bi-grid-1x2-fill"
                      style={{ fontSize: "large" }}
                    ></i>
                    <span className="align-middle" style={{ fontSize: "large" }}>
                      Dashboard
                    </span>
                  </Link>
                </li>
                <li className="sidebar-item">
                  <a
                    className="sidebar-link collapsed d-flex justify-content-between align-items-center"
                    href
                    onClick={toggleCompany}
                    data-bs-target="#company"
                    data-bs-toggle="collapse"
                  >
                    <span className="align-middle">
                      <i class="bi bi-building" style={{ fontSize: "large" }}></i>
                    </span>{" "}
                    <span className="align-middle" style={{ fontSize: "medium" }}>
                      Company
                    </span>
                    <i
                      className={`bi ${
                        isCompanyOpen ? "bi-chevron-up" : "bi-chevron-down"
                      } ms-auto`}
                    ></i>
                  </a>
                  <ul
                    id="company"
                    className={`sidebar-dropDown list-unstyled collapse ${
                      isCompanyOpen ? "show" : ""
                    }`}
                    data-bs-parent="#sidebar"
                  >
                    <li
                      style={{ paddingLeft: "40px" }}
                      className={`sidebar-item ${
                        location.pathname === "/companyRegistration"
                          ? "active"
                          : ""
                      }`}
                    >
                      <Link className="sidebar-link" to={"/companyRegistration"}>
                        Company Registration
                      </Link>
                    </li>
                    <li
                      style={{ paddingLeft: "40px" }}
                      className={`sidebar-item ${
                        location.pathname.startsWith("/companyView")
                          ? "active"
                          : ""
                      }`}
                    >
                      <Link className="sidebar-link" to={"/companyView"}>
                        Company View
                      </Link>
                    </li>
                  </ul>
                </li>
              </>
            )}
            {roles.includes("company_admin") && (
            <>
              <li
                className={`sidebar-item ${
                  location.pathname === "/main" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/main"}>
                  <i
                    class="bi bi-grid-1x2-fill"
                    style={{ fontSize: "large" }}
                  ></i>
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Dashboard
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname === "/department" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/department"}>
                  <i
                    class="bi bi-diagram-3-fill"
                    style={{ fontSize: "large" }}
                  ></i>
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Departments
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname === "/designation" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/designation"}>
                  <i
                    class="bi bi-file-earmark-medical-fill"
                    style={{ fontSize: "large" }}
                  ></i>
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Designation
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/employee") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/employeeview"}>
                  <i
                    class="bi bi-person-plus-fill"
                    style={{ fontSize: "large" }}
                  ></i>
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Employees
                  </span>
                </Link>
              </li>
              {/* <li
                            className={`sidebar-item ${
                              location.pathname.startsWith("/payslip") ? "active" : ""
                            }`}
                          >
                            <Link className="sidebar-link" to={"/payslipview"}>
                              <Receipt color="orange" size={25} />{" "}
                              <i class="bi bi-file-earmark-medical-fill" style={{ fontSize: "large" }}></i>
          
                              <span
                                className="align-middle"
                                style={{ fontSize: "large" }}
                              >
                                PaySlips
                              </span>
                            </Link>
                          </li> */}
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/existing") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/existingList"}>
                  <i
                    class="bi bi-person-x-fill"
                    style={{ fontSize: "large" }}
                  ></i>
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Existing Process
                  </span>
                </Link>
              </li>
              {/* <li
                            className={`sidebar-item ${
                              location.pathname.startsWith("/users") ? "active" : ""
                            }`}
                          >
                            <Link className="sidebar-link" to={"/usersView"}>
                              <i class="bi bi-person-circle" style={{ fontSize: "large" }}></i>
                              <span
                                className="align-middle"
                                style={{ fontSize: "large" }}
                              >
                                Users Summary
                              </span>
                            </Link>
                          </li> */}

              <li className="sidebar-item">
                <a
                  className="sidebar-link collapsed d-flex justify-content-between align-items-center"
                  href
                  onClick={togglePayroll}
                  data-bs-target="#payroll"
                  data-bs-toggle="collapse"
                >
                  <span className="align-middle">
                    <i
                      class="bi bi-receipt-cutoff"
                      style={{ fontSize: "large" }}
                    ></i>
                  </span>{" "}
                  <span className="align-middle" style={{ fontSize: "medium" }}>
                    PayRoll
                  </span>
                  <i
                    className={`bi ${
                      isPayrollOpen ? "bi-chevron-up" : "bi-chevron-down"
                    } ms-auto`}
                  ></i>
                  {/* Add dropdown here */}
                </a>
                <ul
                  id="payroll"
                  className={`sidebar-dropDown list-unstyled collapse ${
                    isPayrollOpen ? "show" : ""
                  }`}
                  data-bs-parent="#sidebar"
                >
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/employeeSalaryStructure"
                        ? "active"
                        : ""
                    }`}
                  >
                    <Link
                      className="sidebar-link"
                      to={"/employeeSalaryStructure"}
                    >
                      Manage Salary
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname.startsWith("/payslipsList")
                        ? "active"
                        : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/payslipsList"}>
                      PaySlips
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/payslipGeneration" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/payslipGeneration"}>
                      Generate PaySlips
                    </Link>
                  </li>
                  {/* <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/companySalaryStructure"
                        ? "active"
                        : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/companySalaryStructure"}>
                      Salary Structure
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/employeeSalaryList" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/employeeSalaryList"}>
                      Salary List
                    </Link>
                  </li> */}
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/increment" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/increment"}>
                      Increments
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/incrementList" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/incrementList"}>
                      Increment List
                    </Link>
                  </li>
                </ul>
              </li>
              <li className="sidebar-item has-dropdown">
                <a
                  className="sidebar-link collapsed d-flex justify-content-between align-items-center"
                  data-bs-target="#attendenceManagement"
                  data-bs-toggle="collapse"
                  href=" "
                  onClick={toggleAttendance}
                >
                  <span className="align-middle">
                    <i
                      class="bi bi-calendar-check-fill"
                      style={{ fontSize: "medium" }}
                    ></i>
                  </span>{" "}
                  <span className="align-middle" style={{ fontSize: "medium" }}>
                    Attendance
                  </span>
                  <i
                    className={`bi ${
                      isAttendanceOpen ? "bi-chevron-up" : "bi-chevron-down"
                    } ms-auto`}
                  ></i>
                </a>
                <ul
                  id="attendenceManagement"
                  className={`sidebar-dropDown list-unstyled collapse ${
                    isAttendanceOpen ? "show" : ""
                  }`}
                >
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/addAttendance" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/addAttendance"}>
                      Manage Attendance
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/attendanceReport" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/attendanceReport"}>
                      Attendance Report
                    </Link>
                  </li>
                  <li
                    style={{ paddingLeft: "40px" }}
                    className={`sidebar-item ${
                      location.pathname === "/attendanceList" ? "active" : ""
                    }`}
                  >
                    <Link className="sidebar-link" to={"/attendanceList"}>
                      Attendance List
                    </Link>
                  </li>
                </ul>
              </li>
            </>
          )}

          {/* {roles === "Employee" && (

            <>
              <li
                className={`sidebar-item ${
                  location.pathname === "/main" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/main"}>
                  <Speedometer2 color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Dashboard
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname === "/department" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/department"}>
                  <Diagram3Fill color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Departments
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname === "/designation" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/designation"}>
                  <FileMedicalFill color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Designation
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/employee") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/employeeview"}>
                  <PersonVcardFill color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Employees
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/payslip") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/payslipview"}>
                  <Receipt color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    PaySlips
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/relieving") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/relievingview"}>
                  <EraserFill color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Relieved Summary
                  </span>
                </Link>
              </li>
            </>
          )} */}

          {roles.includes("Employee") && (

            <>
              <li
                className={`sidebar-item ${
                  location.pathname === "/main" ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/main"}>
                  <Speedometer2 color="orange" size={25} />{" "}
                  <span className="align-middle" style={{ fontSize: "large" }}>
                    Dashboard
                  </span>
                </Link>
              </li>

              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/payslip") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/emppayslip"}>
                  {/* < color="orange" size={25} />{" "} */}
                  <i
                      class="bi bi-card-list"
                      style={{ size: "25", color:"orange" }}
                    ></i>
                  <span className="align-middle " style={{ fontSize: "large" }}>
                    SalaryList
                  </span>
                </Link>
              </li>
              <li
                className={`sidebar-item ${
                  location.pathname.startsWith("/payslip") ? "active" : ""
                }`}
              >
                <Link className="sidebar-link" to={"/employeePayslip"}>
                  <Receipt color="orange" size={25} />{" "}
                  <span className="align-middle " style={{ fontSize: "large" }}>
                    PaySlips
                  </span>
                </Link>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
};


               

  export default SideNav;
