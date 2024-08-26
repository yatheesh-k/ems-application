import React, { useEffect, useState } from 'react';
import LayOut from '../../LayOut/LayOut';
import Select from 'react-select';
import { useForm } from 'react-hook-form';
import { Bounce, toast } from 'react-toastify';
import { EmployeeGetApi, AttendanceReportApi, AttendanceDeleteById, AttendancePatchById } from '../../Utils/Axios';
import { PencilSquare, XSquareFill } from 'react-bootstrap-icons';
import DataTable from 'react-data-table-component';
import { useNavigate } from 'react-router-dom';
import DeletePopup from '../../Utils/DeletePopup';
import { ModalTitle, ModalHeader, ModalBody } from 'react-bootstrap';

const AttendanceReport = () => {
  const { register, handleSubmit, formState: { errors }, reset } = useForm();
  const [employees, setEmployees] = useState([]);
  const [showFields, setShowFields] = useState(false);
  const [selectedMonth, setSelectedMonth] = useState("");
  const [selectedYear, setSelectedYear] = useState("");
  const [selectedEmployeeDetails, setSelectedEmployeeDetails] = useState({});
  const [editAttendance, setEditAttendance] = useState(false);
  const [attendanceData, setAttendanceData] = useState([]);
  const [refreshData, setRefreshData] = useState("");
  const [employeeAttendance, setEmployeeAttendance] = useState([]);
  const [employeeId, setEmployeeId] = useState("");
  const [selectedItemId, setSelectedItemId] = useState(null);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState("");
  const [selectedAttendanceId, setSelectedAttendanceId] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedAttendance, setSelectedAttendance] = useState({});
  const navigate = useNavigate();

  const handleEmployeeChange = (selectedOption) => {
    setEmployeeId(selectedOption.value);
    const selectedEmployee = employees.find(emp => emp.value === selectedOption.value);
    setSelectedEmployeeDetails(selectedEmployee);
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setSelectedItemId(null);
    setSelectedEmployeeId("");
    setSelectedAttendanceId("");
  };

  const handleShowDeleteModal = (row) => {
    setSelectedItemId(row.id);
    setSelectedEmployeeId(row.employeeId);
    setSelectedAttendanceId(row.attendanceId);
    setShowDeleteModal(true);
  };

  const handleShowEditModal = (row) => {
    setSelectedAttendance(row);
    setSelectedEmployeeId(row.employeeId);
    setSelectedAttendanceId(row.attendanceId);
    reset({
      totalWorkingDays: row.totalWorkingDays,
      noOfWorkingDays: row.noOfWorkingDays,
    });
    setEditAttendance(true);
    setShowEditModal(true);
  };

  const handleCloseEditModal = () => {
    setShowEditModal(false);
    setSelectedAttendance({});
  };

  const filterByMonthYear = () => {
    if (employeeId && selectedMonth && selectedYear) {
      fetchAttendanceData(employeeId, selectedMonth, selectedYear);
    } else {
      alert("Please select employee, month, and year.");
    }
  };

  const fetchAttendanceData = async (empId, month, year) => {
    try {
      const monthNames = getMonthNames();
      const monthName = monthNames[month - 1];
      const response = await AttendanceReportApi(empId, monthName, year);
      setAttendanceData(response.data.data);
      setEmployeeAttendance(response.data.data);
      setShowFields(true);
    } catch (error) {
      console.error("Error fetching attendance data:", error);
    }
  };

  useEffect(() => {
    const fetchEmployees = async () => {
      try {
        const data = await EmployeeGetApi();
        const formattedData = data
          .filter((employee) => employee.firstName !== null)
          .map(({ id, firstName, lastName, employeeId }) => ({
            label: `${firstName} ${lastName} (${employeeId})`,
            value: id,
            firstName,
            lastName,
            employeeId,
          }));
        setEmployees(formattedData);
      } catch (error) {
        console.error("Error fetching employees:", error);
      }
    };

    fetchEmployees();
  }, []);

  const getMonthNames = () => {
    return Array.from({ length: 12 }, (_, i) =>
      new Date(0, i).toLocaleString("en-US", { month: "long" })
    );
  };

  const getRecentYears = () => {
    const currentYear = new Date().getFullYear();
    return Array.from({ length: 11 }, (_, i) => (currentYear - i).toString());
  };

  const handleDelete = async () => {
    try {
      await AttendanceDeleteById(selectedEmployeeId, selectedAttendanceId);
      toast.success("Attendance Record Deleted Successfully", {
        position: "top-right",
        transition: Bounce,
        hideProgressBar: true,
        theme: "colored",
        autoClose: 3000,
      });
      setTimeout(() => {
        handleCloseDeleteModal();
        fetchAttendanceData(employeeId, selectedMonth, selectedYear); // Add parameters here
        setRefreshData((prev) => !prev);
      }, 1500);
    } catch (error) {
      handleApiErrors(error);
    }
  };

  const onSubmit = async (data) => {
    console.log("Submitting attendance update for:", selectedEmployeeId, selectedAttendanceId, data);
    try {
      await AttendancePatchById(selectedEmployeeId, selectedAttendanceId, data);
      toast.success("Attendance Record Updated Successfully", {
        position: "top-right",
        transition: Bounce,
        hideProgressBar: true,
        theme: "colored",
        autoClose: 3000,
      });
      setTimeout(() => {
        handleCloseEditModal();
        setRefreshData((prev) => !prev);
        navigate("/attendanceReport");
        fetchAttendanceData(employeeId, selectedMonth, selectedYear); // Add parameters here
      }, 1000);
    } catch (error) {
      handleApiErrors(error);
    }
  };

  const handleApiErrors = (error) => {
    if (error.response && error.response.data && error.response.data.error && error.response.data.error.message) {
      const errorMessage = error.response.data.error.message;
      toast.error(errorMessage);
    } else {
      toast.error("Network Error !");
    }
    console.error(error.response);
  };

  const columns = [
    {
      name: <h6><b>S No</b></h6>,
      selector: (row, index) => index + 1,
      width: "70px",
    },
    {
      name: <h6><b>Month</b></h6>,
      selector: (row) => row.month,
      sortable: true,
      width: "130px",
    },
    {
      name: <h6><b>Year</b></h6>,
      selector: (row) => row.year,
      sortable: true,
      width: "120px",
    },
    {
      name: <h6><b>No. Of Working Days</b></h6>,
      selector: (row) => row.noOfWorkingDays,
      sortable: true,
      width: "240px",
    },
    {
      name: <h6><b>Total Working Days</b></h6>,
      selector: (row) => row.totalWorkingDays,
      sortable: true,
      width: "240px",
    },
    {
      name: <h6><b>Actions</b></h6>,
      cell: (row) => (
        <div>
          <button
            className="btn btn-sm"
            style={{ backgroundColor: "transparent", border: "none", padding: "0", marginLeft: "5px" }}
            onClick={() => handleShowEditModal(row)}
            title='Edit'
          >
            <PencilSquare size={22} color="#2255a4" />
          </button>
          <button
            className="btn btn-sm"
            style={{ backgroundColor: "transparent", border: "none", padding: "0", marginLeft: "5px" }}
            onClick={() => handleShowDeleteModal(row)}
            title='Delete'
          >
            <XSquareFill size={22} color="#da542e" />
          </button>
        </div>
      ),
    },
  ];

  return (
    <LayOut>
      <div className="container-fluid p-0">
        <div className="row d-flex align-items-center justify-content-between mt-1 mb-2">
          <div className="col">
            <h1 className="h3 mb-3">
              <strong>Attendance Report</strong>
            </h1>
          </div>
          <div className="col-auto" style={{ paddingBottom: '20px' }}>
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <a href="/main">Home</a>
                </li>
                <li className="breadcrumb-item active">Attendance</li>
                <li className="breadcrumb-item active">Attendance Report</li>
              </ol>
            </nav>
          </div>
        </div>
        <div className="row">
          <div className="col-12">
            <div className="card">
              <div className="card-header">
                <div className="row d-flex justify-content-center" style={{ paddingLeft: '50px' }}>
                  <div className="col-md-3 mt-3">
                    <label className="form-label">Select Employee</label>
                    <Select
                      options={employees}
                      onChange={handleEmployeeChange}
                      placeholder="Select Employee"
                    />
                  </div>
                  <div className="col-md-3 mt-3">
                    <label className="form-label">Select Year</label>
                    <Select
                      options={getRecentYears().map((year) => ({
                        label: year,
                        value: year,
                      }))}
                      onChange={(selectedOption) => setSelectedYear(selectedOption.value)}
                      placeholder="Select Year"
                    />
                  </div>
                  <div className="col-md-3 mt-3">
                    <label className="form-label">Select Month</label>
                    <Select
                      options={getMonthNames().map((month, index) => ({
                        label: month,
                        value: index + 1,
                      }))}
                      onChange={(selectedOption) => setSelectedMonth(selectedOption.value)}
                      placeholder="Select Month"
                    />
                  </div>
                  <div className="col-md-3 mt-5">
                    <button
                      style={{ paddingBottom: "8px" }}
                      className="btn btn-primary"
                      onClick={filterByMonthYear}
                      disabled={!employeeId || !selectedMonth || !selectedYear}
                    >
                      Go
                    </button>
                  </div>
                </div>
              </div>
              <div className="card-body">
                {showFields && (
                  <div>
                    <h5 className="card-title mt-2">
                      Attendance Details for {`${selectedEmployeeDetails.firstName} ${selectedEmployeeDetails.lastName} (${selectedEmployeeDetails.employeeId})`}
                    </h5>
                    <hr />
                    <div>
                      <DataTable
                        columns={columns}
                        data={employeeAttendance}
                        pagination
                        highlightOnHover
                        pointerOnHover
                        dense
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      <DeletePopup
        show={showDeleteModal}
        handleClose={handleCloseDeleteModal}
        handleConfirm={handleDelete}
        id={selectedItemId}
        pageName="Attendance"
      />
      {showEditModal && (
        <div
          role='dialog'
          aria-modal="true"
          className='fade modal show'
          tabIndex="-1"
          style={{ zIndex: "9999", display: "block" }}
        >
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <ModalHeader>
                <ModalTitle>Edit Attendance</ModalTitle>
              </ModalHeader>
              <ModalBody>
                <form onSubmit={handleSubmit(onSubmit)} >
                  <div className="card-body" style={{ width: "1060px", paddingBottom: "0px" }}>
                    <div className='col-12 col-md-6 col-lg-4 mb-2'>
                      <label>Total Working Days</label>
                      <input
                        type="number"
                        name='totalWorkingDays'
                        readOnly
                        className="form-control"
                        {...register("totalWorkingDays", { required: true })}
                      />
                      {errors.totalWorkingDays && <span>This field is required</span>}
                    </div>
                    <div className='col-12 col-md-6 col-lg-4 mb-2'>
                      <label>No. Of Working Days</label>
                      <input
                        type="number"
                        name='noOfWorkingDays'
                        className="form-control"
                        {...register("noOfWorkingDays", { required: true })}
                      />
                      {errors.noOfWorkingDays && <span>This field is required</span>}
                    </div>
                  </div>
                  <div className='modal-footer'>
                    <button
                      className="btn btn-primary"
                      type='submit'
                    >
                      Save changes
                    </button>
                    <button
                      type='button'
                      className="btn btn-secondary"
                      onClick={handleCloseEditModal}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              </ModalBody>
            </div>
          </div>
        </div>
      )}
    </LayOut>
  );
};

export default AttendanceReport;
