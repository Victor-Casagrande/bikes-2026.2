package br.edu.ifc.bikes.exception;

public class CodigoUniqueViolationException extends RuntimeException{
    public CodigoUniqueViolationException(String message){
        super(message);
    }
}
