package com.example.sahal.Springbootmultithreading2.Feign;

import com.example.sahal.Springbootmultithreading2.dto.CityDto;
import com.example.sahal.Springbootmultithreading2.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.ObjectFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CityFeignFallback implements CityFeignService{

    @Override
    public ResponseEntity<CityDto> findCityByName(String name){
        log.info("Fall back validation");
        CityDto cityDto = new CityDto(0, null, null);
        List<ErrorDto> errors = new ArrayList<>();
        ErrorDto errorDto = new ErrorDto();
        errorDto.setTimeStamp(new Date());
        errorDto.setDetails("Custom Error");
        errorDto.setMessage("City service is down");
        errors.add(errorDto);
        cityDto.setErrors(errors);
        return ResponseEntity.ok(cityDto);
    }

    @Override
    public ResponseEntity<CityDto> findCityById(@PathVariable long id){
        log.info("Fall back validation");
        CityDto cityDto = new CityDto(0, null, null);
        List<ErrorDto> errors = new ArrayList<>();
        ErrorDto errorDto = new ErrorDto();
        errorDto.setTimeStamp(new Date());
        errorDto.setDetails("Custom Error");
        errorDto.setMessage("City service is down");
        errors.add(errorDto);
        cityDto.setErrors(errors);
        return ResponseEntity.ok(cityDto);
    }

}
