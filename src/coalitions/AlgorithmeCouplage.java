package coalitions;

import agents.AgentAcheteur;
import agents.AgentFournisseur;
import models.Service;

import java.util.*;

/**
 * Question 2a – Algorithme de Couplage (mode compétitif, sans partage d'information)
 * ===================================================================================
 *
 * Principe (Algorithme 1, cours C3a – Samir Aknine) :
 *   Chaque fournisseur trie les acheteurs disponibles selon l'utilité jointe
 *   (fournisseur + acheteur) et soumet une offre à son meilleur partenaire.
 *   Si l'acheteur, de son côté, préfère aussi ce fournisseur, une coalition
 *   bilatérale est formée.  Les agents appariés quittent le pool.
 *   Aucune information n'est partagée entre agents du même rôle (mode compétitif).
 *
 * Correspondance avec l'algorithme du cours :
 *   - "Extend Offer"         : fournisseur choisit son meilleur acheteur disponible
 *   - "Receive Offer"        : acheteur compare avec son partenaire courant
 *   - "Send Response"        : si appariement mutuel -> coalition formée
 *   - Itérations répétées jusqu'à stabilisation (aucun nouvel appariement)
 */
public class AlgorithmeCouplage {

    /** Résultat : une coalition bilatérale fournisseur ↔ acheteur. */
    public static class Coalition {
        public final AgentFournisseur fournisseur;
        public final AgentAcheteur   acheteur;
        public final Service         service;
        public final double          valeur;

        Coalition(AgentFournisseur f, AgentAcheteur a, Service s, double v) {
            this.fournisseur = f;
            this.acheteur    = a;
            this.service     = s;
            this.valeur      = v;
        }

        @Override
        public String toString() {
            return String.format("Coalition(%s <-> %s | service=%s | valeur=%.2f)",
                    fournisseur.getNom(), acheteur.getNom(),
                    service != null ? service.getId() : "N/A", valeur);
        }
    }

    // ---- Calcul d'utilité jointe ---------------------------------------

    /**
     * Utilité jointe d'une paire (fournisseur, acheteur) pour un service donné.
     * Prix médian entre le minimum fournisseur et le budget acheteur.
     */
    public static double utilitePaire(AgentFournisseur f, AgentAcheteur a,
                                       Service service) {
        double prixMedian = (f.getContrainte().getPrixMin() +
                             a.getContrainte().getBudgetMax()) / 2.0;
        double uF = f.calculerUtilite(service, prixMedian);
        double uA = a.evaluerOffre(service, prixMedian);
        if (uF == Double.NEGATIVE_INFINITY || uA == Double.NEGATIVE_INFINITY)
            return Double.NEGATIVE_INFINITY;
        return uF + uA;
    }

    /** Meilleure utilité jointe d'un fournisseur vis-à-vis d'un acheteur
     *  (prend le meilleur service disponible). */
    private static double meilleureUtilite(AgentFournisseur f, AgentAcheteur a) {
        double best = Double.NEGATIVE_INFINITY;
        for (Service s : f.getServices()) {
            double u = utilitePaire(f, a, s);
            if (u > best) best = u;
        }
        return best;
    }

    /** Service optimal pour la paire (f, a). */
    private static Service meilleurService(AgentFournisseur f, AgentAcheteur a) {
        Service best = null;
        double  bestU = Double.NEGATIVE_INFINITY;
        for (Service s : f.getServices()) {
            double u = utilitePaire(f, a, s);
            if (u > bestU) { bestU = u; best = s; }
        }
        return best;
    }

    // ---- Algorithme principal ------------------------------------------

    private final List<AgentFournisseur> fournisseurs;
    private final List<AgentAcheteur>    acheteurs;
    private final List<Coalition>        coalitions = new ArrayList<>();

    public AlgorithmeCouplage(List<AgentFournisseur> fournisseurs,
                               List<AgentAcheteur>    acheteurs) {
        this.fournisseurs = new ArrayList<>(fournisseurs);
        this.acheteurs    = new ArrayList<>(acheteurs);
    }

    /**
     * Exécute l'algorithme de couplage.
     *
     * Étapes par itération :
     *  1. Chaque fournisseur libre classe les acheteurs disponibles.
     *  2. Il choisit son meilleur acheteur (utilité jointe maximale).
     *  3. On vérifie que l'acheteur préfère aussi ce fournisseur.
     *  4. Si appariement mutuel -> coalition bilatérale formée.
     *  5. Les deux agents quittent le pool.
     */
    public List<Coalition> executer() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("QUESTION 2a – ALGORITHME DE COUPLAGE (Mode Competitif)");
        System.out.println("=".repeat(60));

        List<AgentFournisseur> libresF = new ArrayList<>(fournisseurs);
        List<AgentAcheteur>    libresA = new ArrayList<>(acheteurs);

        int iteration = 0;
        int maxIter   = fournisseurs.size() * acheteurs.size() + 1;

        while (!libresF.isEmpty() && !libresA.isEmpty() && iteration < maxIter) {
            iteration++;
            System.out.println("\n--- Iteration " + iteration + " ---");

            boolean nouvelleCoalition = false;

            for (AgentFournisseur f : new ArrayList<>(libresF)) {
                if (f.getServices().isEmpty()) { libresF.remove(f); continue; }

                // Étape 1 : classer les acheteurs disponibles par utilité décroissante
                AgentAcheteur meilleurA   = null;
                double        meilleurU   = Double.NEGATIVE_INFINITY;
                Service       meilleurSvc = null;

                for (AgentAcheteur a : libresA) {
                    double u = meilleureUtilite(f, a);
                    if (u > meilleurU) {
                        meilleurU   = u;
                        meilleurA   = a;
                        meilleurSvc = meilleurService(f, a);
                    }
                }

                if (meilleurA == null || meilleurU == Double.NEGATIVE_INFINITY) {
                    System.out.println("  [" + f.getNom() + "] Aucun acheteur compatible.");
                    libresF.remove(f);
                    continue;
                }

                // Étape 2 : vérifier que l'acheteur préfère aussi ce fournisseur
                double uCote = meilleureUtilite(f, meilleurA);
                if (uCote > Double.NEGATIVE_INFINITY) {
                    // Appariement mutuel -> coalition formée
                    Coalition c = new Coalition(f, meilleurA, meilleurSvc, meilleurU);
                    coalitions.add(c);
                    libresF.remove(f);
                    libresA.remove(meilleurA);
                    nouvelleCoalition = true;

                    System.out.printf("  Coalition : %s <-> %s | service=%s | valeur=%.2f%n",
                            f.getNom(), meilleurA.getNom(),
                            meilleurSvc != null ? meilleurSvc.getId() : "N/A",
                            meilleurU);
                    break;  // une seule coalition par fournisseur par itération
                }
            }

            if (!nouvelleCoalition) {
                System.out.println("  Aucun appariement possible -> fin de l'algorithme.");
                break;
            }
        }

        afficherResultats();
        return coalitions;
    }

    // ---- Affichage -----------------------------------------------------

    private void afficherResultats() {
        System.out.println("\n--- Resultats du Couplage ---");
        System.out.println("Nombre de coalitions formees : " + coalitions.size());
        for (int i = 0; i < coalitions.size(); i++)
            System.out.println("  " + (i + 1) + ". " + coalitions.get(i));
        System.out.printf("Valeur totale de la structure : %.2f%n", valeurStructure());
    }

    public double valeurStructure() {
        return coalitions.stream().mapToDouble(c -> c.valeur).sum();
    }

    public List<Coalition> getCoalitions() { return coalitions; }
}
