package models;

import java.time.LocalDate;

/** Contraintes imposées côté acheteur (budget max, date limite, trajet souhaité). */
public class ContrainteAcheteur {

    private final double    budgetMax;
    private final LocalDate dateLimiteAchat;
    private final String    origine;
    private final String    destination;

    public ContrainteAcheteur(double budgetMax, LocalDate dateLimiteAchat,
                               String origine, String destination) {
        this.budgetMax       = budgetMax;
        this.dateLimiteAchat = dateLimiteAchat;
        this.origine         = origine;
        this.destination     = destination;
    }

    public double    getBudgetMax()        { return budgetMax; }
    public LocalDate getDateLimiteAchat()  { return dateLimiteAchat; }
    public String    getOrigine()          { return origine; }
    public String    getDestination()      { return destination; }
}
