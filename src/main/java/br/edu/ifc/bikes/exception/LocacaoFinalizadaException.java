package br.edu.ifc.bikes.exception;

//a locação já teve a devolução registrada
public class LocacaoFinalizadaException extends RuntimeException{
    public LocacaoFinalizadaException(String message){
        super(message);
    }
}
