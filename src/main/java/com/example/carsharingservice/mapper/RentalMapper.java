package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.rental.CreateRentalRequestDto;
import com.example.carsharingservice.dto.rental.RentalResponseDto;
import com.example.carsharingservice.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = {CarMapper.class})
public interface RentalMapper {
    @Mapping(source = "carId", target = "car.id")
    Rental toModel(CreateRentalRequestDto createRentalRequestDto);

    @Mapping(target = "car", source = "car")
    @Mapping(target = "userId", source = "user.id")
    RentalResponseDto toDto(Rental save);
}
