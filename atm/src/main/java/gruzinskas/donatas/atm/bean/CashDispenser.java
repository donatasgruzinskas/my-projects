package gruzinskas.donatas.atm.bean;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.stereotype.Component;

@Component
public interface CashDispenser {
    boolean issueMoney(int amountCents);
}
