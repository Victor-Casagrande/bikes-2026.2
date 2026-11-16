package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.BicicletaCreateDto;
import br.edu.ifc.bikes.dto.BicicletaResponseDto;
import br.edu.ifc.bikes.entity.Bicicleta;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

//id e campos de auditoria não vêm da requisição: são definidos pelo banco
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BicicletaMapper {
    Bicicleta toBicicleta(BicicletaCreateDto bicicletaCreateDto);
    BicicletaResponseDto toResponse(Bicicleta bicicleta);
    List<BicicletaResponseDto> toResponse(List<Bicicleta> bicicletas);
}
