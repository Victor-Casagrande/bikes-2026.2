package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.exception.RelatorioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//Baseado no JasperService do professor, com quatro ajustes:
// 1) a conexão com o banco é fechada ao final (antes, cada relatório prendia uma conexão do pool);
// 2) os parâmetros são criados a cada chamada (antes, um mapa único era compartilhado entre requisições);
// 3) em caso de erro é lançada RelatorioException (antes, devolvia null e o controller falhava sem mensagem);
// 4) IMG_DIR recebe a URL real da pasta reports/ (o Jasper não entende o prefixo "classpath:").
@Slf4j
@RequiredArgsConstructor
@Service
public class JasperService {

    private static final String JASPER_DIR = "classpath:reports/";
    public static final String RELATORIO_CLIENTES = "clientes-bikes";

    private final ResourceLoader resourceLoader;
    private final DataSource dataSource;

    //relatório de clientes (endpoint GET /api/v1/clientes/relatorio)
    public byte[] generatePdf() {
        return generatePdf(RELATORIO_CLIENTES, Map.of());
    }

    //gera qualquer relatório compilado (.jasper) da pasta resources/reports
    public byte[] generatePdf(String relatorio, Map<String, Object> parametros) {
        Map<String, Object> params = new HashMap<>(parametros);
        try (InputStream inputStream = resourceLoader.getResource(JASPER_DIR + relatorio + ".jasper").getInputStream();
             Connection connection = dataSource.getConnection()) {
            //no relatório a imagem do cabeçalho é: $P{IMG_DIR} + "images/bikes2.png"
            params.put("IMG_DIR", resourceLoader.getResource(JASPER_DIR).getURL().toString());
            params.put(JRParameter.REPORT_LOCALE, Locale.of("pt", "BR"));

            JasperPrint print = JasperFillManager.fillReport(inputStream, params, connection);
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception e) {
            log.error("Erro ao gerar o relatório '{}'", relatorio, e);
            throw new RelatorioException(String.format("Não foi possível gerar o relatório '%s'", relatorio), e);
        }
    }
}
