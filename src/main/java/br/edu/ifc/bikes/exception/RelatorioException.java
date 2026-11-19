package br.edu.ifc.bikes.exception;

//falha ao gerar um relatório em PDF (arquivo .jasper ausente, erro na consulta, etc.)
public class RelatorioException extends RuntimeException{
    public RelatorioException(String message, Throwable cause){
        super(message, cause);
    }
}
