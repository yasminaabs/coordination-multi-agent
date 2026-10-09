package models;

import java.time.LocalDate;

/** Contraintes imposées côté fournisseur (prix minimal, date limite de vente). */
public class ContrainteFournisseur {

    private final double    prixMin;
    private final LocalDate dateLimiteVente;

    public ContrainteFournisseur(double prixMin, LocalDate dateLimiteVente) {
        this.prixMin         = prixMin;
        this.dateLimiteVente = dateLimiteVente;
    }

    public double    getPrixMin()          { return prixMin; }
    public LocalDate getDateLimiteVente()  { return dateLimiteVente; }
}
