package gruzinskas.donatas.atm.common;

public class InsufficientFundsException  extends RuntimeException{
    public InsufficientFundsException(String message) {
        super(message);
    }
}
