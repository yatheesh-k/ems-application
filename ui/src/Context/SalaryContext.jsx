import { createContext, useContext, useState, useEffect } from 'react';
import { useLocation, useHistory, useNavigate } from 'react-router-dom';

const SalaryContext = createContext();

export const useSalaryContext = () => useContext(SalaryContext);
export const SalaryProvider = ({ children }) => {
  const location = useLocation();
  const history = useNavigate();
  
  const getInitialFormData = () => {
    const searchParams = new URLSearchParams(location.search);
    const data = searchParams.get('formData');
    return data ? JSON.parse(data) : {
      allowances: {
        hra: 0,
        travelAllowance: 0,
        specialAllowance: 0,
        otherAllowances: 0,
      },
      deductions: {
        pfEmployee: 0,
      },
    };
  };

  const [formData, setFormData] = useState(getInitialFormData);

  const updateFormData = (newData) => {
    setFormData(newData);
    const searchParams = new URLSearchParams(location.search);
    searchParams.set('formData', JSON.stringify(newData));
    history.replace({ search: searchParams.toString() });
  };

  return (
    <SalaryContext.Provider value={{ formData, updateFormData }}>
      {children}
    </SalaryContext.Provider>
  );
};
