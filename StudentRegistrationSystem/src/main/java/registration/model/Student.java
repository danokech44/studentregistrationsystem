package registration.model;

import java.math.BigDecimal;

public class Student extends Person {
    private BigDecimal feeBalance;

    public Student(String name, String studentNumber, BigDecimal feeBalance) {
        super(name, studentNumber);
        this.feeBalance = feeBalance;
    }

    public BigDecimal getFeeBalance() { return feeBalance; }
    public void setFeeBalance(BigDecimal feeBalance) { this.feeBalance = feeBalance; }

    public boolean hasClearedFees() {
        return feeBalance.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return super.toString() + " | Balance: $" + feeBalance;
    }
}