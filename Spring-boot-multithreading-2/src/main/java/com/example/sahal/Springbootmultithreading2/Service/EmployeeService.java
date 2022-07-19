package com.example.sahal.Springbootmultithreading2.Service;

import com.example.sahal.Springbootmultithreading2.Exception.ResourceNotFoundException;
import com.example.sahal.Springbootmultithreading2.Feign.CityFeignService;
import com.example.sahal.Springbootmultithreading2.Feign.CompanyFeignService;
import com.example.sahal.Springbootmultithreading2.Model.Employee;
import com.example.sahal.Springbootmultithreading2.Repository.EmployeeRepository;
import com.example.sahal.Springbootmultithreading2.ValueObject.ResponseValueObject;
import com.example.sahal.Springbootmultithreading2.dto.CityDto;
import com.example.sahal.Springbootmultithreading2.dto.CompanyDto;
import com.example.sahal.Springbootmultithreading2.dto.EmployeeDto;
import com.example.sahal.Springbootmultithreading2.dto.ErrorDto;
import com.example.sahal.Springbootmultithreading2.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private CityFeignService cityFeignService;

    @Autowired
    private CompanyFeignService companyFeignService;

    @Async
    public CompletableFuture<String> saveEmployeesThroughFile(MultipartFile file) throws Exception{
        List<Employee> employeeList = parseCSVFile(file);
        log.info("Saving list of employees of size "+ employeeList.size()+" "+Thread.currentThread().getName());
        employeeRepository.saveAll(employeeList);
        String message = "Data saved successfully!";
        return CompletableFuture.completedFuture(message);
    }

    @Async
    public CompletableFuture<List<EmployeeDto>> findAllEmployees() {
        List<EmployeeDto> employeeDtoList;
        try {
            log.info("Getting list of employees by "+Thread.currentThread().getName());
            List<Employee> employeeList = employeeRepository.findAll()
                    .stream()
                    .filter(Employee::isActive)
                    .collect(Collectors.toList());
            employeeDtoList = employeeMapper.entityToDto(employeeList);
        }
        catch (Exception ex){
            log.error("Exception caught "+ex.getMessage());
            throw ex;
        }
        return CompletableFuture.completedFuture(employeeDtoList);
    }

    @Async
    public CompletableFuture<Employee> findAllEmployeesByThread(long id) {
        //List<Employee> employeeList = new ArrayList<>();
            Employee employee = employeeRepository.findById(id).get();
            //employeeList.add(employee);
            log.info("Finding employees by thread "+Thread.currentThread().getName());
        return CompletableFuture.completedFuture(employee);
    }

    private List<Employee> parseCSVFile(MultipartFile file) throws Exception{
        final List<Employee> employeeList = new ArrayList<>();
        try {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] data = line.split(",");
                    final Employee employee = new Employee();
                    employee.setFirstName(data[0]);
                    employee.setLastName(data[1]);
                    employee.setEmail(data[2]);
                    employee.setGender(data[3]);
                    employee.setCompanyId(Long.parseLong(data[4]));
                    employee.setJobTitle(data[5]);
                    employee.setRegistrationId(Long.parseLong(data[6]));
                    employee.setCityId(Long.parseLong(data[7]));
                    employee.setActive(true);
                    employeeList.add(employee);
                }
                return employeeList;
            }
        }
        catch (Exception ex){
            log.error("Failed to parse CSV file "+ex);
            throw ex;
        }
    }

    @Async
    public CompletableFuture<ResponseValueObject> findEmployeeById(long id) throws Exception {
        Employee employee = employeeRepository.findById(id)
                .filter(Employee::isActive)
                .orElseThrow(()-> new ResourceNotFoundException("Employee not found with id "+id));
        EmployeeDto employeeDto = employeeMapper.entityToDto(employee);
        CityDto cityDto = cityFeignService.findCityById(employeeDto.getCityId()).getBody();
        CompanyDto companyDto = companyFeignService.findCompanyById(employeeDto.getCompanyId()).getBody();
        ResponseValueObject vo = new ResponseValueObject();
        List<EmployeeDto> employeeDtoList = new ArrayList<>();
        employeeDtoList.add(employeeDto);
        vo.setCity(cityDto);
        vo.setCompany(companyDto);
        vo.setEmployeeList(employeeDtoList);
        return CompletableFuture.completedFuture(vo);
    }

    @Async
    public CompletableFuture<ResponseValueObject> findEmployeesByCityName(String name) throws ExecutionException, InterruptedException {
        ResponseValueObject vo = new ResponseValueObject();
        List<Employee> employeeList = new ArrayList<>();
        List<EmployeeDto> employeeDtoList = new ArrayList<>();
        List<ErrorDto> errors = new ArrayList<>();
        CityDto cityDto = cityFeignService.findCityByName(name).getBody();
        if (cityDto != null && cityDto.getErrors() == null) {
            long cityId = cityDto.getId();
            employeeList = employeeRepository.findAllByCityId(cityId)
                    .stream()
                    .filter(Employee::isActive)
                    .collect(Collectors.toList());
            employeeDtoList = employeeMapper.entityToDto(employeeList);
        }
        else if (cityDto != null && cityDto.getErrors() !=null){
            EmployeeDto employeeDto = new EmployeeDto();
            ErrorDto error = new ErrorDto();
            error.setMessage(cityDto.getErrors().get(0).getMessage());
            error.setDetails(cityDto.getErrors().get(0).getDetails());
            error.setTimeStamp(cityDto.getErrors().get(0).getTimeStamp());
            errors.add(error);
            employeeDto.setErrors(errors);
            employeeDtoList.add(employeeDto);
        }
        vo.setCity(cityDto);
        vo.setEmployeeList(employeeDtoList);
        return CompletableFuture.completedFuture(vo);
    }

    @Async
    public CompletableFuture<ResponseValueObject> findEmployeesByCompanyName(String name) throws ExecutionException, InterruptedException {
        ResponseValueObject vo = new ResponseValueObject();
        List<Employee> employeeList = new ArrayList<>();
        List<EmployeeDto> employeeDtoList = new ArrayList<>();
        CompanyDto companyDto = companyFeignService.findCompanyByName(name).getBody();
        long companyId = companyDto.getId();
        employeeList = employeeRepository.findAllByCompanyId(companyId)
                .stream()
                .filter(Employee::isActive)
                .collect(Collectors.toList());
        employeeDtoList = employeeMapper.entityToDto(employeeList);
        vo.setCompany(companyDto);
        vo.setEmployeeList(employeeDtoList);
        return CompletableFuture.completedFuture(vo);
    }

    @Async
    public CompletableFuture<String> saveEmployee(EmployeeDto employeeDto) {
        Employee employee = employeeMapper.dtoToEntity(employeeDto);
        employee.setActive(true);
        String result = null;
        try {
            employeeRepository.save(employee);
            result = "Employee created successfully";
        }
        catch (Exception ex){
            result = "Exception caught "+ex;
            throw ex;
        }
        finally {
            return CompletableFuture.completedFuture(result);
        }
    }

    @Async
    public CompletableFuture<String> updateEmployee(long id, EmployeeDto employeeDto) throws Exception{
        boolean isEmployeeAlreadyPresent = employeeRepository.findById(id)
                .filter(Employee::isActive)
                .isPresent();
        Employee updatedEmployee = employeeMapper.dtoToEntity(employeeDto);
        updatedEmployee.setActive(true);
        String result;
        if(isEmployeeAlreadyPresent){
            updatedEmployee.setId(id);
            employeeRepository.save(updatedEmployee);
            result = "Employee with id "+id+" updated successfully";
        }
        else {
            //result = "employee with id "+id+" does not exist, therefore new employee is created";
            throw new  ResourceNotFoundException("Employee with id "+id+" does not exist!");
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<Boolean> deleteEmployee(long id) {
        Optional<Employee> employee = employeeRepository.findById(id)
                .filter(Employee::isActive);
        boolean isDeleted = false;
        if (employee.isPresent()){
            employee.get().setActive(false);
            employeeRepository.save(employee.get());
            isDeleted = true;
        }
        return CompletableFuture.completedFuture(isDeleted);
    }
}
