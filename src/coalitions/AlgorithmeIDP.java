package coalitions;

import agents.Agent;
import agents.AgentAcheteur;
import agents.AgentFournisseur;

import java.util.*;

/**
 * IDP – Iterative Dynamic Programming (Yun Yeh, 1986)
 * =====================================================
 *
 * Trouve la structure de coalitions SC* = argmax V(SC) par programmation
 * dynamique en traitant les coalitions par taille croissante.
 *
 * Algorithme (cours C3b – Samir Aknine) :
 *
 *   Etape 1 – Singletons :
 *     Pour chaque agent ai : V[{ai}] = v({ai}), P[{ai}] = null
 *
 *   Etape 2 – Pour k = 2, 3, ..., n :
 *     Pour chaque coalition C de taille k :
 *       best = v(C)      (valeur de C prise comme un tout)
 *       Pour chaque bipartition (S, C\S) avec |S| de 1 a k/2 :
 *         Si V[S] + V[C\S] > best :
 *           best = V[S] + V[C\S]
 *           P[C] = (S, C\S)
 *       V[C] = best
 *
 *   Etape 3 – Reconstruction récursive à partir de P[grand_coalition].
 *
 * Complexité : O(3^n) évaluations, O(2^n) espace mémoire.
 *
 * Partage d'information : limité – les agents partagent uniquement
 * leurs valeurs de coalitions (pas leurs stratégies internes).
 */
public class AlgorithmeIDP {

    // ---- Fonction caractéristique par défaut ---------------------------

    /**
     * v(C) : valeur d'une coalition.
     * Pour une coalition mixte (fournisseurs + acheteurs) :
     *   somme des utilités jointes des paires compatibles,
     *   avec un bonus coopératif de 5 % par membre supplémentaire.
     * Pour une coalition homogène : valeurs individuelles réduites.
     */
    public static double valeurCoalition(Set<Agent> membres) {
        List<AgentFournisseur> fs = new ArrayList<>();
        List<AgentAcheteur>    as = new ArrayList<>();

        for (Agent a : membres) {
            if (a instanceof AgentFournisseur) fs.add((AgentFournisseur) a);
            else if (a instanceof AgentAcheteur)  as.add((AgentAcheteur)  a);
        }

        if (!fs.isEmpty() && !as.isEmpty()) {
            // Coalition mixte
            double valeur = 0.0;
            for (AgentFournisseur f : fs)
                for (AgentAcheteur a : as)
                    for (var service : f.getServices()) {
                        double u = AlgorithmeCouplage.utilitePaire(f, a, service);
                        if (u != Double.NEGATIVE_INFINITY) valeur += Math.max(0, u);
                    }
            double bonus = 0.05 * Math.max(0, membres.size() - 2);
            return valeur * (1.0 + bonus);
        }
        if (!fs.isEmpty())   // coalition homogène fournisseurs
            return fs.stream().mapToDouble(f -> f.getContrainte().getPrixMin() * 0.10).sum();
        // coalition homogène acheteurs
        return as.stream().mapToDouble(a -> a.getContrainte().getBudgetMax() * 0.05).sum();
    }

    // ---- Structures de données DP --------------------------------------

    /**
     * Représente un sous-ensemble d'agents par un masque binaire (index dans la liste).
     * Permet d'utiliser une Map<Integer, ...> au lieu d'une Map<Set, ...>.
     */

    private final List<Agent>               agents;
    private final int                       n;
    private final Map<Integer, Double>      V = new HashMap<>();   // masque -> valeur
    private final Map<Integer, int[]>       P = new HashMap<>();   // masque -> [s1, s2]
    private       int                       nbEvaluations = 0;

    public AlgorithmeIDP(List<Agent> agents) {
        this.agents = agents;
        this.n      = agents.size();
    }

    // ---- Évaluation mémoïsée -------------------------------------------

    private double v(int masque) {
        if (!V.containsKey(masque)) {
            nbEvaluations++;
            Set<Agent> membres = masqueVersEnsemble(masque);
            V.put(masque, valeurCoalition(membres));
        }
        return V.get(masque);
    }

    // ---- Exécution principale ------------------------------------------

    /**
     * Exécute IDP et retourne la structure de coalitions optimale
     * sous forme d'une liste d'ensembles d'agents.
     */
    public List<Set<Agent>> executer() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALGORITHME IDP (Programmation Dynamique Iterative)");
        System.out.println("=".repeat(60));

        int grandCoalition = (1 << n) - 1;

        // Etape 1 : singletons
        System.out.println("\nEtape 1 – Singletons :");
        for (int i = 0; i < n; i++) {
            int masque = 1 << i;
            double val = v(masque);
            P.put(masque, null);
            System.out.printf("  V[{%s}] = %.2f%n", agents.get(i).getNom(), val);
        }

        // Etape 2 : tailles croissantes
        for (int taille = 2; taille <= n; taille++) {
            System.out.printf("%nEtape 2 – Taille %d :%n", taille);

            // Énumérer tous les sous-ensembles de taille k
            for (int masque : sousEnsemblesDeT(taille, n)) {
                double bestVal   = v(masque);   // valeur de la coalition entière
                int[]  bestSplit = null;

                // Essayer toutes les bipartitions (S, masque \ S)
                // On ne parcourt que les sous-ensembles de taille 1 à k/2 pour éviter les doublons
                List<Integer> sousEnsMasque = sousEnsemblesStricts(masque);
                for (int s1 : sousEnsMasque) {
                    if (Integer.bitCount(s1) > taille / 2) continue;
                    // Pour k pair, éviter le doublon (S1,S2) et (S2,S1) quand |S1|==|S2|
                    int s2 = masque & ~s1;
                    if (Integer.bitCount(s1) == Integer.bitCount(s2) && s1 > s2) continue;

                    double combined = V.getOrDefault(s1, v(s1))
                                    + V.getOrDefault(s2, v(s2));
                    if (combined > bestVal) {
                        bestVal   = combined;
                        bestSplit = new int[]{s1, s2};
                    }
                }

                V.put(masque, bestVal);
                P.put(masque, bestSplit);

                String nomC = nomsCoalition(masque);
                if (bestSplit != null) {
                    System.out.printf("  V[{%s}] = %.2f  ->  {%s} + {%s}%n",
                            nomC, bestVal,
                            nomsCoalition(bestSplit[0]),
                            nomsCoalition(bestSplit[1]));
                } else {
                    System.out.printf("  V[{%s}] = %.2f  ->  coalition entiere%n",
                            nomC, bestVal);
                }
            }
        }

        // Etape 3 : reconstruction
        List<Set<Agent>> structure = reconstruire(grandCoalition);
        double valeurTotale = V.getOrDefault(grandCoalition, 0.0);

        afficherResultats(structure, valeurTotale);
        return structure;
    }

    // ---- Reconstruction ------------------------------------------------

    private List<Set<Agent>> reconstruire(int masque) {
        int[] split = P.get(masque);
        if (split == null) {
            List<Set<Agent>> res = new ArrayList<>();
            res.add(masqueVersEnsemble(masque));
            return res;
        }
        List<Set<Agent>> res = new ArrayList<>();
        res.addAll(reconstruire(split[0]));
        res.addAll(reconstruire(split[1]));
        return res;
    }

    // ---- Utilitaires ---------------------------------------------------

    private Set<Agent> masqueVersEnsemble(int masque) {
        Set<Agent> res = new LinkedHashSet<>();
        for (int i = 0; i < n; i++)
            if ((masque & (1 << i)) != 0) res.add(agents.get(i));
        return res;
    }

    /** Tous les sous-ensembles de taille exactement t parmi n bits. */
    private static List<Integer> sousEnsemblesDeT(int t, int n) {
        List<Integer> res = new ArrayList<>();
        // Gosper's hack pour énumérer les sous-ensembles de taille t
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

    /** Tous les sous-ensembles non vides et stricts d'un masque. */
    private static List<Integer> sousEnsemblesStricts(int masque) {
        List<Integer> res = new ArrayList<>();
        for (int s = (masque - 1) & masque; s > 0; s = (s - 1) & masque)
            res.add(s);
        return res;
    }

    private String nomsCoalition(int masque) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if ((masque & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(agents.get(i).getNom());
            }
        }
        return sb.toString();
    }

    // ---- Affichage -----------------------------------------------------

    private void afficherResultats(List<Set<Agent>> structure, double valeur) {
        System.out.println("\n--- Resultats IDP ---");
        for (int i = 0; i < structure.size(); i++) {
            Set<Agent> c    = structure.get(i);
            int        mask = ensembleVersMasque(c);
            System.out.printf("  Coalition %d : {%s}  valeur = %.2f%n",
                    i + 1, nomsCoalition(mask), V.getOrDefault(mask, 0.0));
        }
        System.out.printf("Valeur totale : %.2f%n", valeur);
        System.out.printf("Nombre d'evaluations de v : %d%n", nbEvaluations);
    }

    private int ensembleVersMasque(Set<Agent> ensemble) {
        int masque = 0;
        for (Agent a : ensemble)
            masque |= (1 << agents.indexOf(a));
        return masque;
    }

    // ---- Accesseurs ----------------------------------------------------

    public int    getNbEvaluations() { return nbEvaluations; }
    public double getValeurOptimale() {
        return V.getOrDefault((1 << n) - 1, 0.0);
    }
}
