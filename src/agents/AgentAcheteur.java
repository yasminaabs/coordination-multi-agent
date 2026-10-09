package agents;

import models.*;
import negotiation.StrategieNegociation;

import java.time.LocalDate;
import java.util.*;

/**
 * Agent acheteur : personne ou organisme cherchant à acheter un billet de voyage.
 *
 * Responsabilités :
 *   - Évaluer les offres selon ses contraintes (budget, trajet, date)
 *     et ses préférences (compagnies préférées/refusées, mode de transport)
 *   - Soumettre des contre-offres en suivant la stratégie de négociation
 *   - Accepter dès que le prix est satisfaisant
 */
public class AgentAcheteur extends Agent {

    private final ContrainteAcheteur  contrainte;
    private final PreferenceAcheteur  preference;
    private final StrategieNegociation strategie;

    private final Map<String, NegociationEtat> negociations = new HashMap<>();
    private final List<AchatRecord>            achats       = new ArrayList<>();
    private double budgetRestant;

    // ---- Constructeur --------------------------------------------------

    public AgentAcheteur(String agentId, String nom,
                          ContrainteAcheteur contrainte,
                          PreferenceAcheteur preference,
                          StrategieNegociation strategie) {
        super(agentId, nom);
        this.contrainte     = contrainte;
        this.preference     = preference;
        this.strategie      = strategie;
        this.budgetRestant  = contrainte.getBudgetMax();
    }

    // ---- Raisonnement individuel ----------------------------------------

    /**
     * Calcule l'utilité d'une offre reçue.
     * Retourne Double.NEGATIVE_INFINITY si une contrainte dure n'est pas respectée.
     */
    public double evaluerOffre(Service service, double prix) {
        if (prix > contrainte.getBudgetMax())                   return Double.NEGATIVE_INFINITY;
        if (!service.getDestination().equals(contrainte.getDestination())) return Double.NEGATIVE_INFINITY;
        if (!service.getOrigine().equals(contrainte.getOrigine()))         return Double.NEGATIVE_INFINITY;
        if (preference.getCompagniesRefusees().contains(service.getCompagnie())) return Double.NEGATIVE_INFINITY;
        if (LocalDate.now().isAfter(contrainte.getDateLimiteAchat()))      return Double.NEGATIVE_INFINITY;

        double utilite = contrainte.getBudgetMax() - prix;   // économies réalisées

        if (preference.getCompagniesPreferees().contains(service.getCompagnie()))
            utilite *= 1.20;   // +20 % pour compagnie préférée
        if (service.getTypeService().equals(preference.getTypeServicePrefere()))
            utilite *= 1.10;   // +10 % pour mode de transport préféré

        return utilite;
    }

    // ---- Démarrage -----------------------------------------------------

    @Override
    public void demarrerNegociation() {
        System.out.printf("[%s] Pret a negocier. Budget: %.2f EUR, Trajet: %s -> %s%n",
                nom, contrainte.getBudgetMax(),
                contrainte.getOrigine(), contrainte.getDestination());
    }

    // ---- Traitement des messages entrants ------------------------------

    @Override
    protected void traiterMessage(Message message) {
        switch (message.getType()) {
            case OFFRE        -> traiterOffre(message);
            case ACCEPTATION  -> traiterAcceptation(message);
            case REFUS        -> traiterRefus(message);
            default -> {}
        }
    }

    private void traiterOffre(Message message) {
        Service service = message.getService();
        double  prix    = message.getPrix();
        double  utilite = evaluerOffre(service, prix);

        System.out.printf("[%s] Offre recue : %s a %.2f EUR (utilite=%.2f)%n",
                nom, service.getId(), prix, utilite);

        if (utilite == Double.NEGATIVE_INFINITY) {
            refuser(message, "Contraintes non satisfaites");
            return;
        }

        // Acceptation directe si le prix est dans la cible initiale
        if (prix <= strategie.getValeurInitiale()) {
            accepter(message, service, prix);
            return;
        }

        // Contre-offre
        NegociationEtat neg = negociations.get(message.getNegociationId());
        if (neg == null) {
            neg = new NegociationEtat(service, message.getExpediteurId(),
                                       strategie.getValeurInitiale());
            negociations.put(message.getNegociationId(), neg);
        } else {
            neg.iteration++;
            neg.prixCourant = strategie.prochaineOffreAcheteur(
                    neg.prixCourant, neg.iteration);
            neg.nbPropositions++;
        }

        if (neg.nbPropositions > strategie.getMaxPropositions()) {
            if (prix <= contrainte.getBudgetMax()) accepter(message, service, prix);
            else refuser(message, "Max propositions atteint");
            return;
        }

        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                TypeMessage.CONTRE_OFFRE, service, neg.prixCourant,
                message.getNegociationId()));
        System.out.printf("[%s] Contre-offre (%d) : %s a %.2f EUR%n",
                nom, neg.nbPropositions, service.getId(), neg.prixCourant);
    }

    private void traiterAcceptation(Message message) {
        budgetRestant -= message.getPrix();
        achats.add(new AchatRecord(message.getService(), message.getPrix(),
                                    message.getExpediteurId()));
        negociations.remove(message.getNegociationId());
        System.out.printf("[%s] Contre-offre acceptee par le fournisseur : %s a %.2f EUR%n",
                nom, message.getService().getId(), message.getPrix());
    }

    private void traiterRefus(Message message) {
        NegociationEtat neg = negociations.remove(message.getNegociationId());
        if (neg != null)
            System.out.printf("[%s] Refus recu pour %s%n", nom, neg.service.getId());
    }

    // ---- Helpers -------------------------------------------------------

    private void accepter(Message message, Service service, double prix) {
        budgetRestant -= prix;
        achats.add(new AchatRecord(service, prix, message.getExpediteurId()));
        negociations.remove(message.getNegociationId());
        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                TypeMessage.ACCEPTATION, service, prix, message.getNegociationId()));
        System.out.printf("[%s] Accepte : %s a %.2f EUR%n", nom, service.getId(), prix);
    }

    private void refuser(Message message, String raison) {
        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                TypeMessage.REFUS, message.getService(), 0,
                message.getNegociationId(), raison));
    }

    // ---- Rapport -------------------------------------------------------

    public void afficherRapport() {
        double total = achats.stream().mapToDouble(a -> a.prix).sum();
        System.out.printf("  %-22s | achats=%-2d | depense=%.2f/%.2f EUR | restant=%.2f EUR%n",
                nom, achats.size(), total, contrainte.getBudgetMax(), budgetRestant);
        for (AchatRecord a : achats)
            System.out.printf("       %s achete a %.2f EUR%n", a.service.getId(), a.prix);
    }

    // ---- Accesseurs (pour les algorithmes de coalition) ----------------

    public ContrainteAcheteur  getContrainte()   { return contrainte; }
    public PreferenceAcheteur  getPreference()   { return preference; }
    public StrategieNegociation getStrategie()   { return strategie; }
    public boolean              aAchete()        { return !achats.isEmpty(); }

    // ---- Classes internes ----------------------------------------------

    private static class NegociationEtat {
        Service service;
        String  fournisseurId;
        double  prixCourant;
        int     nbPropositions = 1;
        int     iteration      = 0;

        NegociationEtat(Service service, String fournisseurId, double prix) {
            this.service       = service;
            this.fournisseurId = fournisseurId;
            this.prixCourant   = prix;
        }
    }

    private static class AchatRecord {
        Service service;
        double  prix;
        String  fournisseurId;
        AchatRecord(Service service, double prix, String fournisseurId) {
            this.service       = service;
            this.prix          = prix;
            this.fournisseurId = fournisseurId;
        }
    }
}
