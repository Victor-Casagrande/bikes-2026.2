package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.LocacaoRelatorioDto;
import br.edu.ifc.bikes.dto.LocacaoResponseDto;
import br.edu.ifc.bikes.entity.Locacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//a locação é montada pelo service (cliente, bicicleta e datas não vêm da requisição),
//por isso o mapper só converte a entidade para as respostas
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocacaoMapper {
    @Mapping(source = "cliente.nome", target = "nomeCliente")
    @Mapping(source = "bicicleta.codigo", target = "codigoBicicleta")
    LocacaoResponseDto toResponse(Locacao locacao);

    List<LocacaoResponseDto> toResponse(List<Locacao> locacoes);

    @Mapping(source = "cliente.nome", target = "nomeCliente")
    @Mapping(source = "bicicleta.codigo", target = "codigoBicicleta")
    LocacaoRelatorioDto toRelatorio(Locacao locacao);

    List<LocacaoRelatorioDto> toRelatorio(List<Locacao> locacoes);
}
