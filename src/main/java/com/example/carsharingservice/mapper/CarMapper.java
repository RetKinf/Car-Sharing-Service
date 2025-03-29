package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.car.CarResponseDto;
import com.example.carsharingservice.dto.car.CreateCarRequestDto;
import com.example.carsharingservice.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    Car toModel(CreateCarRequestDto requestDto);

    @Named("carToDto")
    CarResponseDto toDto(Car car);
}
