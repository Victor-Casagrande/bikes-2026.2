package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.ClienteCreateDto;
import br.edu.ifc.bikes.dto.ClienteResponseDto;
import br.edu.ifc.bikes.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

//id, usuário e campos de auditoria não vêm da requisição: são definidos pelo service e pelo banco
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {
    Cliente toCliente(ClienteCreateDto clienteCreateDto);
    ClienteResponseDto toResponse(Cliente cliente);
    List<ClienteResponseDto> toResponse(List<Cliente> clientes);
}
