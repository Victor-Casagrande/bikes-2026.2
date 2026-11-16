package br.edu.ifc.bikes.exception;

//não há bicicleta livre para alugar, ou a bicicleta está alugada e não pode mudar de status
public class BicicletaIndisponivelException extends RuntimeException{
    public BicicletaIndisponivelException(String message){
        super(message);
    }
}
