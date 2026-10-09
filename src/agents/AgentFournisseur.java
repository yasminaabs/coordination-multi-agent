package agents;

import models.*;
import negotiation.StrategieNegociation;

import java.time.LocalDate;
import java.util.*;

/**
 * Agent fournisseur : compagnie aérienne / ferroviaire.
 *
 * Responsabilités :
 *   - Exposer une liste de services (billets) disponibles à la vente
 *   - Appliquer les contraintes du fournisseur (prix minimal, date limite)
 *   - Mener des négociations bilatérales avec les agents acheteurs
 *   - Réduire progressivement son prix selon la stratégie de négociation
 */
public class AgentFournisseur extends Agent {

    private final List<Service>            services;
    private final ContrainteFournisseur    contrainte;
    private final PreferenceFournisseur    preference;
    private final StrategieNegociation     strategie;

    // negociationId -> état de la négociation
    private final Map<String, NegociationEtat> negociations = new HashMap<>();

    // résultats finaux
    private final List<VenteRecord> ventesConclues = new ArrayList<>();

    // ---- Constructeur --------------------------------------------------

    public AgentFournisseur(String agentId, String nom,
                             List<Service> services,
                             ContrainteFournisseur contrainte,
                             PreferenceFournisseur preference,
                             StrategieNegociation strategie) {
        super(agentId, nom);
        this.services   = new ArrayList<>(services);
        this.contrainte = contrainte;
        this.preference = preference;
        this.strategie  = strategie;
    }

    // ---- Raisonnement individuel ----------------------------------------

    /**
     * Calcule l'utilité de vendre un service au prix donné.
     * Retourne Double.NEGATIVE_INFINITY si une contrainte dure n'est pas satisfaite.
     */
    public double calculerUtilite(Service service, double prix) {
        if (prix < contrainte.getPrixMin())               return Double.NEGATIVE_INFINITY;
        if (LocalDate.now().isAfter(contrainte.getDateLimiteVente())) return Double.NEGATIVE_INFINITY;

        double gain  = prix - service.getPrixBase();
        double bonus = 0.0;
        if (preference.getDateSouhaiteeVente() != null &&
                !LocalDate.now().isAfter(preference.getDateSouhaiteeVente())) {
            bonus = gain * 0.10;   // +10 % si vente avant date souhaitée
        }
        return gain + bonus;
    }

    // ---- Démarrage -----------------------------------------------------

    @Override
    public void demarrerNegociation() {
        System.out.println("[" + nom + "] Services disponibles :");
        for (Service s : services) System.out.println("  - " + s);
    }

    // ---- Interaction : soumettre une offre -----------------------------

    /**
     * Envoie une offre initiale pour un service à un acheteur identifié.
     */
    public void soumettreOffre(Service service, String acheteurId) {
        soumettreOffre(service, acheteurId, strategie.getValeurInitiale());
    }

    public void soumettreOffre(Service service, String acheteurId, double prix) {
        if (!services.contains(service)) return;

        String negId = agentId + "_" + service.getId() + "_" + System.nanoTime() % 100000;
        negociations.put(negId, new NegociationEtat(service, acheteurId, prix));

        Message msg = new Message(agentId, acheteurId,
                TypeMessage.OFFRE, service, prix, negId);
        envoyerMessage(msg);
        System.out.printf("[%s] Offre -> %s : %s a %.2f EUR%n",
                nom, acheteurId, service.getId(), prix);
    }

    // ---- Traitement des messages entrants ------------------------------

    @Override
    protected void traiterMessage(Message message) {
        NegociationEtat neg = negociations.get(message.getNegociationId());

        switch (message.getType()) {

            case ACCEPTATION:
                if (neg != null) {
                    finaliserVente(neg.service, neg.prixCourant,
                                   message.getNegociationId());
                    System.out.printf("[%s] Vente conclue : %s a %.2f EUR -> %s%n",
                            nom, neg.service.getId(), neg.prixCourant,
                            message.getExpediteurId());
                }
                break;

            case REFUS:
                if (neg != null) {
                    if (neg.nbPropositions < strategie.getMaxPropositions()) {
                        faireNouvelleOffre(neg, message);
                    } else {
                        System.out.printf("[%s] Negociation abandonnee (max propositions) : %s%n",
                                nom, neg.service.getId());
                        negociations.remove(message.getNegociationId());
                    }
                }
                break;

            case CONTRE_OFFRE:
                if (neg != null) {
                    double prixPropose = message.getPrix();
                    if (calculerUtilite(neg.service, prixPropose) != Double.NEGATIVE_INFINITY) {
                        finaliserVente(neg.service, prixPropose,
                                       message.getNegociationId());
                        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                                TypeMessage.ACCEPTATION, neg.service, prixPropose,
                                message.getNegociationId()));
                        System.out.printf("[%s] Contre-offre acceptee : %s a %.2f EUR%n",
                                nom, neg.service.getId(), prixPropose);
                    } else {
                        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                                TypeMessage.REFUS, neg.service, 0,
                                message.getNegociationId(), "Prix sous le minimum"));
                    }
                }
                break;

            default:
                break;
        }
    }

    // ---- Helpers -------------------------------------------------------

    private void faireNouvelleOffre(NegociationEtat neg, Message message) {
        neg.iteration++;
        double nouveauPrix = strategie.prochaineOffreFournisseur(
                neg.prixCourant, neg.iteration);

        if (calculerUtilite(neg.service, nouveauPrix) == Double.NEGATIVE_INFINITY) {
            System.out.printf("[%s] Prix plancher atteint pour %s%n",
                    nom, neg.service.getId());
            negociations.remove(message.getNegociationId());
            return;
        }
        neg.prixCourant = nouveauPrix;
        neg.nbPropositions++;

        envoyerMessage(new Message(agentId, message.getExpediteurId(),
                TypeMessage.OFFRE, neg.service, nouveauPrix,
                message.getNegociationId()));
        System.out.printf("[%s] Nouvelle offre (%d) : %s a %.2f EUR%n",
                nom, neg.nbPropositions, neg.service.getId(), nouveauPrix);
    }

    private void finaliserVente(Service service, double prix, String negId) {
        ventesConclues.add(new VenteRecord(service, prix));
        services.remove(service);
        negociations.remove(negId);
    }

    // ---- Rapport -------------------------------------------------------

    public void afficherRapport() {
        double total = ventesConclues.stream().mapToDouble(v -> v.prix).sum();
        System.out.printf("  %-22s | vendus=%-2d | restants=%-2d | total=%.2f EUR%n",
                nom, ventesConclues.size(), services.size(), total);
        for (VenteRecord v : ventesConclues)
            System.out.printf("       %s vendu a %.2f EUR%n", v.service.getId(), v.prix);
    }

    // ---- Accesseurs (pour les algorithmes de coalition) ----------------

    public List<Service>         getServices()    { return services; }
    public ContrainteFournisseur getContrainte()  { return contrainte; }
    public PreferenceFournisseur getPreference()  { return preference; }
    public StrategieNegociation  getStrategie()   { return strategie; }
    public boolean               aVendu()         { return !ventesConclues.isEmpty(); }

    // ---- Classes internes ----------------------------------------------

    private static class NegociationEtat {
        Service service;
        String  acheteurId;
        double  prixCourant;
        int     nbPropositions = 1;
        int     iteration      = 0;

        NegociationEtat(Service service, String acheteurId, double prix) {
            this.service     = service;
            this.acheteurId  = acheteurId;
            this.prixCourant = prix;
        }
    }

    private static class VenteRecord {
        Service service;
        double  prix;
        VenteRecord(Service service, double prix) {
            this.service = service;
            this.prix    = prix;
        }
    }
}
