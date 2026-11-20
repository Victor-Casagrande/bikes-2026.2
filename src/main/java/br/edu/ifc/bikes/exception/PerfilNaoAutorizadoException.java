package br.edu.ifc.bikes.exception;

//tentativa de criar um ADMIN sem ser administrador (quando já existe um admin no sistema)
public class PerfilNaoAutorizadoException extends RuntimeException{
    public PerfilNaoAutorizadoException(String message){
        super(message);
    }
}
