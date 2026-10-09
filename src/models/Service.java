package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Représente un billet de voyage (avion ou train) mis en vente par un fournisseur.
 */
public class Service {

    private final String id;
    private final String typeService;   // "avion" ou "train"
    private final String origine;
    private final String destination;
    private final LocalDate dateDepart;
    private final LocalDate dateArrivee;
    private final String compagnie;
    private final double prixBase;      // coût de base / prix plancher interne

    public Service(String id, String typeService, String origine, String destination,
                   LocalDate dateDepart, LocalDate dateArrivee,
                   String compagnie, double prixBase) {
        this.id          = id;
        this.typeService = typeService;
        this.origine     = origine;
        this.destination = destination;
        this.dateDepart  = dateDepart;
        this.dateArrivee = dateArrivee;
        this.compagnie   = compagnie;
        this.prixBase    = prixBase;
    }

    // ---- Getters -------------------------------------------------------

    public String getId()          { return id; }
    public String getTypeService() { return typeService; }
    public String getOrigine()     { return origine; }
    public String getDestination() { return destination; }
    public LocalDate getDateDepart()  { return dateDepart; }
    public LocalDate getDateArrivee() { return dateArrivee; }
    public String getCompagnie()   { return compagnie; }
    public double getPrixBase()    { return prixBase; }

    // ---- Affichage -----------------------------------------------------

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Service(%s, %s, %s->%s, %s, %.2f EUR, %s)",
                id, typeService, origine, destination,
                dateDepart.format(fmt), prixBase, compagnie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;
        return id.equals(((Service) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
