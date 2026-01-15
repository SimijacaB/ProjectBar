package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    Inventory toEntity(InventoryDTO dto);

    @Mapping(target = "name", ignore = true)
    InventoryResponseDTO toResponseDTO(Inventory inventory);

    List<InventoryResponseDTO> toResponseDTOList(List<Inventory> inventories);
}

