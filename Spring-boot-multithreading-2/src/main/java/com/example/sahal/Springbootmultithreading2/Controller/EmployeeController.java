package com.example.sahal.Springbootmultithreading2.Controller;

import com.example.sahal.Springbootmultithreading2.Model.Employee;
import com.example.sahal.Springbootmultithreading2.Service.EmployeeService;
import com.example.sahal.Springbootmultithreading2.ValueObject.ResponseValueObject;
import com.example.sahal.Springbootmultithreading2.dto.EmployeeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.example.sahal.Springbootmultithreading2.constant.Constant.*;

@RestController
@Slf4j
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;

    @PostMapping(value = "/employees", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity<String> saveEmployeesThroughFile(@Valid @RequestParam(value = "files")MultipartFile[] files) throws Exception{
        CompletableFuture<String> result = null;
        for (MultipartFile file : files){
            result = employeeService.saveEmployeesThroughFile(file);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.get());
    }

    @PostMapping("/employee")
    public ResponseEntity<String> saveEmployee(
            @Valid @RequestBody EmployeeDto employeeDto) throws ExecutionException, InterruptedException {
        CompletableFuture<String> result = employeeService.saveEmployee(employeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result.get());
    }

    @PutMapping("/employee/{id}")
    public ResponseEntity<String> updateEmployee(
            @Positive(message = ID_SHOULD_BE_POSITIVE_ERROR_MESSAGE)
            @PathVariable long id, @Valid @RequestBody EmployeeDto employeeDto) throws Exception{
        String result;
        try {
            result = employeeService.updateEmployee(id, employeeDto).get();
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        catch (Exception ex){
            log.error("Exception caught "+ex.getMessage());
            throw ex;
        }
    }

    @GetMapping(value = "/employees")
    public ResponseEntity<List<EmployeeDto>> findAllEmployees() throws ExecutionException, InterruptedException {
        List<EmployeeDto> employeeDtoList = employeeService.findAllEmployees().get();
        return ResponseEntity.ok(employeeDtoList);
    }

    @GetMapping(value = "/employee/{id}")
    public ResponseEntity<ResponseValueObject> findEmployeeById(@Positive(message = ID_SHOULD_BE_POSITIVE_ERROR_MESSAGE) @PathVariable long id) throws Exception {
        //CompletableFuture<EmployeeDto> employeeDto;
        ResponseValueObject vo;
        try {
            vo = employeeService.findEmployeeById(id).get();
        }
        catch (Exception e){
            throw e;
        }
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/employees/city/{name}")
    public ResponseEntity<ResponseValueObject> findEmployeesByCityName(@PathVariable String name) throws ExecutionException, InterruptedException {
        ResponseValueObject vo;
        try {
            vo = employeeService.findEmployeesByCityName(name).get();
        }
        catch (Exception ex){
            throw ex;
        }
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/employees/company/{name}")
    public ResponseEntity<ResponseValueObject> findEmployeesByCompanyName(@PathVariable String name) throws ExecutionException, InterruptedException {
        ResponseValueObject vo;
        try {
            vo = employeeService.findEmployeesByCompanyName(name).get();
        }
        catch (Exception ex){
            throw ex;
        }
        return ResponseEntity.ok(vo);
    }

    @DeleteMapping("/employee/{id}")
    public ResponseEntity deleteEmployee(@Positive(message = ID_SHOULD_BE_POSITIVE_ERROR_MESSAGE) @PathVariable long id) throws ExecutionException, InterruptedException {
        Boolean isDeleted = employeeService.deleteEmployee(id).get();
        HttpStatus status;
        String message;
        if (isDeleted){
            status = HttpStatus.OK;
            message = "Employee with id "+id+" deleted successfully";
        }
        else {
            status = HttpStatus.NOT_FOUND;
            message = "Employee with id "+id+" not found";
        }
        return ResponseEntity.status(status).body(message);
    }

    @GetMapping(value = "employees/thread")
    public ResponseEntity<List<CompletableFuture<Employee>>> findAllEmployeesByThread(){
        List<CompletableFuture<Employee>> listCompletableFuture = null;
        CompletableFuture<Employee> employeeCompletableFuture;
        for (long i =1 ; i<=3000; i++) {
            Employee employee = new Employee();
            employeeCompletableFuture = employeeService.findAllEmployeesByThread(i);
            listCompletableFuture.add(employeeCompletableFuture);
            //return employeeService.findAllEmployeesByThread().thenApply(ResponseEntity::ok);
        }
        return new ResponseEntity<List<CompletableFuture<Employee>>>(listCompletableFuture, HttpStatus.OK);
    }
}
