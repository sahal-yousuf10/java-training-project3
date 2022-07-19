package com.example.sahal.Springbootmultithreading2.Repository;

import com.example.sahal.Springbootmultithreading2.Model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

    List<Employee> findAllByCityId(long id);
    List<Employee> findAllByCompanyId(long id);
}
