package coalitions;

import agents.Agent;

import java.util.*;

/**
 * IP – Integer Partition / Anytime Tree Search (Rahwan et al., 2009)
 * ==================================================================
 *
 * Algorithme de recherche arborescente "anytime" pour la génération
 * de la structure de coalitions optimale (cours C3b – Samir Aknine).
 *
 * Principe :
 *   L'espace de recherche est organisé en sous-espaces définis par les
 *   partitions entières de n (le nombre d'agents).
 *   Ex. pour n=4 : [4], [3,1], [2,2], [2,1,1], [1,1,1,1].
 *
 *   1. Calculer une BORNE SUPERIEURE pour chaque sous-espace :
 *      borne([p1,...,pk]) = somme des meilleures valeurs de coalitions
 *                           de chaque taille pi.
 *
 *   2. Trier les sous-espaces par borne décroissante.
 *
 *   3. Explorer les sous-espaces dans cet ordre :
 *      - Si borne(sous-espace) <= meilleure solution courante -> ELAGUER.
 *      - Sinon explorer toutes les structures du sous-espace.
 *
 *   Propriété anytime : à tout instant la meilleure solution connue est valide.
 */
public class AlgorithmeIP {

    private final List<Agent>               agents;
    private final int                       n;
    private final Map<Integer, Double>      cache       = new HashMap<>();
    private       int                       nbEvaluations = 0;

    // Meilleure solution trouvée jusqu'ici
    private List<Set<Agent>>  bestStructure = null;
    private double            bestValue     = Double.NEGATIVE_INFINITY;

    public AlgorithmeIP(List<Agent> agents) {
        this.agents = agents;
        this.n      = agents.size();
    }

    // ---- Évaluation mémoïsée -------------------------------------------

    private double v(int masque) {
        return cache.computeIfAbsent(masque, m -> {
            nbEvaluations++;
            return AlgorithmeIDP.valeurCoalition(masqueVersEnsemble(m));
        });
    }

    // ---- Algorithme principal ------------------------------------------

    public List<Set<Agent>> executer() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALGORITHME IP (Partitions Entieres – Anytime)");
        System.out.println("=".repeat(60));

        List<List<Integer>> partitions = partitionsEentieres(n);

        System.out.printf("%nSous-espaces pour %d agents :%n", n);
        for (List<Integer> p : partitions) System.out.println("  " + p);

        // Calcul des bornes supérieures
        List<Map.Entry<List<Integer>, Double>> bornes = new ArrayList<>();
        for (List<Integer> p : partitions)
            bornes.add(Map.entry(p, borneSuperieure(p)));

        bornes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        System.out.println("\nBornes superieures (ordre decroissant) :");
        for (var e : bornes)
            System.out.printf("  %s  ->  %.2f%n", e.getKey(), e.getValue());

        // Exploration
        System.out.println("\nExploration des sous-espaces :");
        for (var entry : bornes) {
            List<Integer> partition = entry.getKey();
            double        borne     = entry.getValue();

            if (borne <= bestValue) {
                System.out.printf("  %s  ELAGUE (borne %.2f <= meilleur %.2f)%n",
                        partition, borne, bestValue);
                continue;
            }

            explorerSousEspace(partition);
            System.out.printf("  %s  ->  valeur=%.2f%s%n",
                    partition, bestValue,
                    bestValue == v((1 << n) - 1)
                            && partition.size() == 1 ? "  ** nouvelle meilleure **" : "");
        }

        afficherResultats();
        return bestStructure != null ? bestStructure : Collections.emptyList();
    }

    // ---- Borne supérieure d'un sous-espace -----------------------------

    private double borneSuperieure(List<Integer> partition) {
        double borne = 0.0;
        for (int taille : partition) {
            double bestForSize = Double.NEGATIVE_INFINITY;
            for (int masque : sousEnsemblesDeT(taille, n)) {
                double val = v(masque);
                if (val > bestForSize) bestForSize = val;
            }
            borne += bestForSize;
        }
        return borne;
    }

    // ---- Exploration d'un sous-espace ----------------------------------

    private void explorerSousEspace(List<Integer> partition) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) ids.add(i);

        // Trier les tailles décroissantes pour générer les partitions dans l'ordre
        List<Integer> tailles = new ArrayList<>(partition);
        tailles.sort(Collections.reverseOrder());

        // Génération récursive de toutes les structures du sous-espace
        genererStructures(ids, tailles, new ArrayList<>());
    }

    private void genererStructures(List<Integer> restants,
                                    List<Integer> tailles,
                                    List<Integer> structCourante) {
        if (tailles.isEmpty()) {
            double val = structCourante.stream()
                                       .mapToDouble(this::v)
                                       .sum();
            if (val > bestValue) {
                bestValue     = val;
                bestStructure = new ArrayList<>();
                for (int masque : structCourante)
                    bestStructure.add(masqueVersEnsemble(masque));
            }
            return;
        }

        int taille    = tailles.get(0);
        List<Integer> taillesRest = tailles.subList(1, tailles.size());

        for (int masque : sousEnsemblesDeT(taille, n)) {
            // Vérifier que ce masque est un sous-ensemble des agents restants
            int restantsMasque = restants.stream()
                                          .reduce(0, (acc, i) -> acc | (1 << i));
            if ((masque & restantsMasque) != masque) continue;

            List<Integer> nouveauxRestants = new ArrayList<>();
            for (int i : restants)
                if ((masque & (1 << i)) == 0) nouveauxRestants.add(i);

            List<Integer> nouvelleStruct = new ArrayList<>(structCourante);
            nouvelleStruct.add(masque);

            genererStructures(nouveauxRestants, taillesRest, nouvelleStruct);
        }
    }

    // ---- Partitions entières -------------------------------------------

    /** Génère toutes les partitions entières de n. */
    private static List<List<Integer>> partitionsEentieres(int n) {
        List<List<Integer>> result = new ArrayList<>();
        genererPartitions(n, n, new ArrayList<>(), result);
        return result;
    }

    private static void genererPartitions(int n, int max,
                                           List<Integer> courante,
                                           List<List<Integer>> result) {
        if (n == 0) { result.add(new ArrayList<>(courante)); return; }
        for (int i = Math.min(n, max); i >= 1; i--) {
            courante.add(i);
            genererPartitions(n - i, i, courante, result);
            courante.remove(courante.size() - 1);
        }
    }

    // ---- Utilitaires ---------------------------------------------------

    /** Tous les sous-ensembles de taille exactement t parmi n bits (Gosper's hack). */
    private static List<Integer> sousEnsemblesDeT(int t, int n) {
        List<Integer> res = new ArrayList<>();
        int masque = (1 << t) - 1;
        int limit  = 1 << n;
        while (masque < limit) {
            res.add(masque);
            int c = masque & -masque;
            int r = masque + c;
            masque = (((r ^ masque) >> 2) / c) | r;
        }
        return res;
    }

    private Set<Agent> masqueVersEnsemble(int masque) {
        Set<Agent> res = new LinkedHashSet<>();
        for (int i = 0; i < n; i++)
            if ((masque & (1 << i)) != 0) res.add(agents.get(i));
        return res;
    }

    private String nomsCoalition(Set<Agent> c) {
        StringBuilder sb = new StringBuilder();
        for (Agent a : c) { if (sb.length() > 0) sb.append(", "); sb.append(a.getNom()); }
        return sb.toString();
    }

    // ---- Affichage -----------------------------------------------------

    private void afficherResultats() {
        System.out.println("\n--- Resultats IP ---");
        if (bestStructure != null) {
            for (int i = 0; i < bestStructure.size(); i++) {
                Set<Agent> c    = bestStructure.get(i);
                int        mask = 0;
                for (Agent a : c) mask |= (1 << agents.indexOf(a));
                System.out.printf("  Coalition %d : {%s}  valeur = %.2f%n",
                        i + 1, nomsCoalition(c), v(mask));
            }
        }
        System.out.printf("Valeur totale : %.2f%n", bestValue);
        System.out.printf("Nombre d'evaluations de v : %d%n", nbEvaluations);
    }

    // ---- Accesseurs ----------------------------------------------------

    public int    getNbEvaluations()  { return nbEvaluations; }
    public double getValeurOptimale() { return bestValue; }
}
