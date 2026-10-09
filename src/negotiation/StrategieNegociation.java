package negotiation;

/**
 * Paramètres de la stratégie de négociation d'un agent.
 *
 * Règles du sujet :
 *   - max 6 soumissions de propositions par négociation.
 *   - une offre ne peut varier de plus de 10 % par itération.
 */
public class StrategieNegociation {

    private final double valeurInitiale;       // prix de départ soumis
    private final int    maxPropositions;      // plafond de propositions (<= 6)
    private final double tauxMaxVariation;     // variation max par itération (<= 0.10)

    public StrategieNegociation(double valeurInitiale,
                                 int maxPropositions,
                                 double tauxMaxVariation) {
        this.valeurInitiale   = valeurInitiale;
        this.maxPropositions  = Math.min(maxPropositions, 6);
        this.tauxMaxVariation = Math.min(tauxMaxVariation, 0.10);
    }

    /**
     * Prochaine offre d'un fournisseur : baisse progressive du prix.
     * Le taux de baisse croît avec les itérations, plafonné à tauxMaxVariation.
     */
    public double prochaineOffreFournisseur(double prixCourant, int iteration) {
        double taux = Math.min(tauxMaxVariation, 0.03 * iteration);
        return Math.round(prixCourant * (1.0 - taux) * 100.0) / 100.0;
    }

    /**
     * Prochaine offre d'un acheteur : hausse progressive du prix proposé.
     * Le taux de hausse croît avec les itérations, plafonné à tauxMaxVariation.
     */
    public double prochaineOffreAcheteur(double prixCourant, int iteration) {
        double taux = Math.min(tauxMaxVariation, 0.03 * iteration);
        return Math.round(prixCourant * (1.0 + taux) * 100.0) / 100.0;
    }

    // ---- Getters -------------------------------------------------------

    public double getValeurInitiale()    { return valeurInitiale; }
    public int    getMaxPropositions()   { return maxPropositions; }
    public double getTauxMaxVariation()  { return tauxMaxVariation; }
}
