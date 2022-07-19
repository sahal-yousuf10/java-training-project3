package com.example.CompanyService.Service;

import com.example.CompanyService.Dto.CompanyDto;
import com.example.CompanyService.Mapper.CompanyMapper;
import com.example.CompanyService.Model.Company;
import com.example.CompanyService.Repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMapper companyMapper;

    @Async
    public CompletableFuture<String> saveCompany(CompanyDto companyDto) {
        Company company = companyMapper.dtoToEntity(companyDto);
        String result = null;
        try {
            companyRepository.save(company);
            result = "Company created successfully";
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
    public CompletableFuture<String> updateCompany(long id, CompanyDto companyDto) throws Exception {

        boolean isCompanyAlreadyPresent = companyRepository.findById(id).isPresent();
        Company updatedCompany = companyMapper.dtoToEntity(companyDto);
        String result;
        if(isCompanyAlreadyPresent){
            updatedCompany.setId(id);
            companyRepository.save(updatedCompany);
            result = "Company with id "+id+" updated successfully";
        }
        else {
            throw new Exception("Company with id "+id+" does not exist!");
        }
        return CompletableFuture.completedFuture(result);
    }

    @Async
    public CompletableFuture<List<CompanyDto>> findAllCompanies() {

        List<CompanyDto> companyDtoList;
        try {
            log.info("Getting list of companies by "+Thread.currentThread().getName());
            List<Company> companyList = companyRepository.findAll();
            companyDtoList = companyMapper.entityToDto(companyList);
        }
        catch (Exception ex){
            log.error("Exception caught "+ex.getMessage());
            throw ex;
        }
        return CompletableFuture.completedFuture(companyDtoList);
    }

    @Async
    public CompletableFuture<CompanyDto> findCompanyById(long id) throws Exception {

        Company company = companyRepository.findById(id)
                .orElseThrow(()-> new Exception("Company not found with id "+id));
        CompanyDto companyDto = companyMapper.entityToDto(company);
        return CompletableFuture.completedFuture(companyDto);
    }

    @Async
    public CompletableFuture<CompanyDto> findCompanyByName(String name) throws Exception {

        Company company = companyRepository.findByName(name)
                .orElseThrow(()-> new Exception("Company not found with name "+name));
        CompanyDto companyDto = companyMapper.entityToDto(company);
        return CompletableFuture.completedFuture(companyDto);
    }
}
