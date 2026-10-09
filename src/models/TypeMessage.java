package models;

/** Types de messages échangés entre agents lors de la négociation. */
public enum TypeMessage {
    OFFRE,          // fournisseur soumet un prix
    DEMANDE,        // acheteur formule une demande
    ACCEPTATION,    // accord sur le prix proposé
    REFUS,          // rejet de l'offre ou contre-offre
    CONTRE_OFFRE    // contre-proposition de prix
}
