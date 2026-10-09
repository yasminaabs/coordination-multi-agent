package models;

import java.time.LocalDateTime;

/**
 * Unité de communication entre agents.
 * Contient l'expéditeur, le destinataire, le type, le service concerné,
 * le prix proposé et l'identifiant de la négociation en cours.
 */
public class Message {

    private final String       expediteurId;
    private final String       destinataireId;
    private final TypeMessage  type;
    private final Service      service;        // peut être null pour REFUS/INFO
    private final double       prix;
    private final String       negociationId;
    private final String       raison;         // motif d'un REFUS
    private final LocalDateTime timestamp;

    // Constructeur complet
    public Message(String expediteurId, String destinataireId,
                   TypeMessage type, Service service, double prix,
                   String negociationId, String raison) {
        this.expediteurId   = expediteurId;
        this.destinataireId = destinataireId;
        this.type           = type;
        this.service        = service;
        this.prix           = prix;
        this.negociationId  = negociationId;
        this.raison         = raison;
        this.timestamp      = LocalDateTime.now();
    }

    // Constructeur simplifié (sans raison)
    public Message(String expediteurId, String destinataireId,
                   TypeMessage type, Service service, double prix,
                   String negociationId) {
        this(expediteurId, destinataireId, type, service, prix, negociationId, "");
    }

    // ---- Getters -------------------------------------------------------

    public String      getExpediteurId()   { return expediteurId; }
    public String      getDestinatireId()  { return destinataireId; }
    public TypeMessage getType()           { return type; }
    public Service     getService()        { return service; }
    public double      getPrix()           { return prix; }
    public String      getNegociationId()  { return negociationId; }
    public String      getRaison()         { return raison; }

    @Override
    public String toString() {
        return String.format("Message(%s: %s -> %s, service=%s, prix=%.2f)",
                type, expediteurId, destinataireId,
                service != null ? service.getId() : "null", prix);
    }
}
