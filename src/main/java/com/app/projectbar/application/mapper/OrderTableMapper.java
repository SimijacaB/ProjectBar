package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.OrderTable;
import com.app.projectbar.domain.dto.orderTable.OrderTableRequestDTO;
import com.app.projectbar.domain.dto.orderTable.OrderTableResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderTableMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    OrderTable toEntity(OrderTableRequestDTO dto);

    OrderTableResponseDTO toResponseDTO(OrderTable orderTable);

    List<OrderTableResponseDTO> toResponseDTOList(List<OrderTable> orderTables);
}

