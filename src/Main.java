import agents.Agent;
import agents.AgentAcheteur;
import agents.AgentFournisseur;
import coalitions.AlgorithmeCouplage;
import coalitions.AlgorithmeIDP;
import coalitions.AlgorithmeIP;
import models.*;
import negotiation.Environnement;
import negotiation.StrategieNegociation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TP1 – Coordination Multi-Agent : Système de Négociation Automatique de Voyage
 * ==============================================================================
 * Auteur : Yasmina Abbas
 *
 * Q1  – Modélisation des agents et simulation d'une négociation bilatérale
 * Q2a – Algorithme de couplage (mode compétitif, sans partage d'information)
 * Q2b – Algorithmes IDP et IP (mode coopératif, partage limité)
 */
public class Main {

    // ===================================================================
    // Création du scénario
    // ===================================================================

    /**
     * Construit un scénario de voyage avec 4 fournisseurs et 4 acheteurs.
     * Retourne [fournisseurs, acheteurs] sous forme d'un tableau de 2 listes.
     */
    @SuppressWarnings("unchecked")
    static List<?>[] creerScenario() {
        LocalDate today = LocalDate.now();

        // ---- Services ---------------------------------------------------
        Service s1 = new Service("VOL-001", "avion", "Paris", "New York",
                today.plusDays(30), today.plusDays(37), "AirFrance", 400);

        Service s2 = new Service("VOL-002", "avion", "Paris", "New York",
                today.plusDays(35), today.plusDays(42), "Delta", 350);

        Service s3 = new Service("TRAIN-01", "train", "Paris", "Lyon",
                today.plusDays(7), today.plusDays(7), "SNCF", 60);

        Service s4 = new Service("VOL-003", "avion", "Lyon", "Barcelone",
                today.plusDays(14), today.plusDays(21), "Iberia", 200);

        Service s5 = new Service("VOL-004", "avion", "Paris", "New York",
                today.plusDays(40), today.plusDays(47), "AirFrance", 420);

        // ---- Agents fournisseurs ----------------------------------------
        AgentFournisseur f1 = new AgentFournisseur("F1", "AgentAirFrance",
                Arrays.asList(s1, s5),
                new ContrainteFournisseur(380, today.plusDays(25)),
                new PreferenceFournisseur(today.plusDays(15)),
                new StrategieNegociation(550, 6, 0.08));

        AgentFournisseur f2 = new AgentFournisseur("F2", "AgentDelta",
                Collections.singletonList(s2),
                new ContrainteFournisseur(320, today.plusDays(30)),
                new PreferenceFournisseur(today.plusDays(20)),
                new StrategieNegociation(480, 5, 0.10));

        AgentFournisseur f3 = new AgentFournisseur("F3", "AgentSNCF",
                Collections.singletonList(s3),
                new ContrainteFournisseur(50, today.plusDays(5)),
                new PreferenceFournisseur(),
                new StrategieNegociation(80, 4, 0.05));

        AgentFournisseur f4 = new AgentFournisseur("F4", "AgentIberia",
                Collections.singletonList(s4),
                new ContrainteFournisseur(180, today.plusDays(10)),
                new PreferenceFournisseur(),
                new StrategieNegociation(280, 6, 0.10));

        // ---- Agents acheteurs -------------------------------------------
        AgentAcheteur a1 = new AgentAcheteur("A1", "AgentAlice",
                new ContrainteAcheteur(600, today.plusDays(20), "Paris", "New York"),
                new PreferenceAcheteur(
                        Arrays.asList("AirFrance"), Collections.emptyList(), "avion"),
                new StrategieNegociation(480, 6, 0.08));

        AgentAcheteur a2 = new AgentAcheteur("A2", "AgentBob",
                new ContrainteAcheteur(450, today.plusDays(28), "Paris", "New York"),
                new PreferenceAcheteur(
                        Arrays.asList("Delta"), Arrays.asList("AirFrance"), "avion"),
                new StrategieNegociation(380, 5, 0.10));

        AgentAcheteur a3 = new AgentAcheteur("A3", "AgentCarole",
                new ContrainteAcheteur(100, today.plusDays(4), "Paris", "Lyon"),
                new PreferenceAcheteur(
                        Arrays.asList("SNCF"), Collections.emptyList(), "train"),
                new StrategieNegociation(70, 4, 0.05));

        AgentAcheteur a4 = new AgentAcheteur("A4", "AgentDavid",
                new ContrainteAcheteur(300, today.plusDays(10), "Lyon", "Barcelone"),
                new PreferenceAcheteur(
                        Collections.emptyList(), Collections.emptyList(), "avion"),
                new StrategieNegociation(240, 6, 0.10));

        List<AgentFournisseur> fournisseurs = Arrays.asList(f1, f2, f3, f4);
        List<AgentAcheteur>    acheteurs    = Arrays.asList(a1, a2, a3, a4);

        return new List<?>[]{ fournisseurs, acheteurs };
    }

    // ===================================================================
    // Question 1 – Modélisation et négociation bilatérale
    // ===================================================================

    static void question1() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("QUESTION 1 – Modelisation des Agents et Negociation");
        System.out.println("=".repeat(60));

        List<?>[] scenario    = creerScenario();
        @SuppressWarnings("unchecked")
        List<AgentFournisseur> fournisseurs = (List<AgentFournisseur>) scenario[0];
        @SuppressWarnings("unchecked")
        List<AgentAcheteur>    acheteurs    = (List<AgentAcheteur>)    scenario[1];

        // Enregistrement dans l'environnement
        Environnement env = new Environnement();
        for (Agent a : fournisseurs) env.enregistrerAgent(a);
        for (Agent a : acheteurs)    env.enregistrerAgent(a);

        // Annonce des agents
        System.out.println("\n-- Demarrage des agents --");
        fournisseurs.forEach(AgentFournisseur::demarrerNegociation);
        acheteurs.forEach(AgentAcheteur::demarrerNegociation);

        // Négociation bilatérale
        System.out.println("\n-- Phase de negociation bilaterale --");
        for (AgentFournisseur f : fournisseurs) {
            for (Service service : new ArrayList<>(f.getServices())) {
                for (AgentAcheteur a : acheteurs) {
                    // Ignorer les acheteurs ayant déjà effectué un achat
                    if (a.aAchete()) continue;
                    if (a.evaluerOffre(service, f.getStrategie().getValeurInitiale())
                            != Double.NEGATIVE_INFINITY) {
                        f.soumettreOffre(service, a.getAgentId());
                        // Exécuter plusieurs cycles de messages
                        for (int i = 0; i <= f.getStrategie().getMaxPropositions(); i++)
                            env.executerCycle();
                        break;  // un seul acheteur par service
                    }
                }
            }
        }

        // Cycles finaux pour vider les boîtes
        for (int i = 0; i < 3; i++) env.executerCycle();

        // Rapports
        System.out.println("\n-- Rapports Finaux --");
        System.out.println("\nFournisseurs :");
        fournisseurs.forEach(AgentFournisseur::afficherRapport);
        System.out.println("\nAcheteurs :");
        acheteurs.forEach(AgentAcheteur::afficherRapport);
        System.out.println();
        env.afficherStatistiques();
    }

    // ===================================================================
    // Question 2a – Algorithme de couplage
    // ===================================================================

    static void question2a() {
        List<?>[] scenario    = creerScenario();   // agents neufs
        @SuppressWarnings("unchecked")
        List<AgentFournisseur> fournisseurs = (List<AgentFournisseur>) scenario[0];
        @SuppressWarnings("unchecked")
        List<AgentAcheteur>    acheteurs    = (List<AgentAcheteur>)    scenario[1];

        AlgorithmeCouplage algo = new AlgorithmeCouplage(fournisseurs, acheteurs);
        algo.executer();
        System.out.printf("%nValeur de la structure (couplage) : %.2f%n",
                algo.valeurStructure());
    }

    // ===================================================================
    // Question 2b – IDP et IP
    // ===================================================================

    static void question2b() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("QUESTION 2b – Algorithmes IDP et IP (Mode Cooperatif)");
        System.out.println("=".repeat(60));

        List<?>[] scenario    = creerScenario();   // agents neufs
        @SuppressWarnings("unchecked")
        List<AgentFournisseur> fournisseurs = (List<AgentFournisseur>) scenario[0];
        @SuppressWarnings("unchecked")
        List<AgentAcheteur>    acheteurs    = (List<AgentAcheteur>)    scenario[1];

        // Sous-ensemble de 2 fournisseurs + 2 acheteurs
        List<Agent> agentsTest = new ArrayList<>();
        agentsTest.add(fournisseurs.get(0));   // AgentAirFrance
        agentsTest.add(fournisseurs.get(1));   // AgentDelta
        agentsTest.add(acheteurs.get(0));      // AgentAlice
        agentsTest.add(acheteurs.get(1));      // AgentBob

        System.out.println("Agents : " + agentsTest.stream()
                .map(Agent::getNom).toList());

        // IDP
        AlgorithmeIDP idp = new AlgorithmeIDP(agentsTest);
        idp.executer();

        // IP
        AlgorithmeIP ip = new AlgorithmeIP(agentsTest);
        ip.executer();

        // Tableau comparatif
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("%-10s %12s %15s%n", "Algo", "Valeur", "Evaluations");
        System.out.println("-".repeat(40));
        System.out.printf("%-10s %12.2f %15d%n", "IDP", idp.getValeurOptimale(), idp.getNbEvaluations());
        System.out.printf("%-10s %12.2f %15d%n", "IP",  ip.getValeurOptimale(),  ip.getNbEvaluations());
        System.out.println("=".repeat(60));

        boolean memeValeur = Math.abs(idp.getValeurOptimale() - ip.getValeurOptimale()) < 1e-6;
        System.out.println("Meme valeur optimale ? " + (memeValeur ? "Oui" : "Non"));

        System.out.println("\n-- Analyse --");
        if (ip.getNbEvaluations() < idp.getNbEvaluations())
            System.out.println("IP est plus efficace grace a l'elagage par bornes superieures.");
        else
            System.out.println("IDP et IP ont une efficacite similaire sur ce petit exemple.");
    }

    // ===================================================================
    // Point d'entrée
    // ===================================================================

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("TP1 – Coordination Multi-Agent : Negociation de Voyages");
        System.out.println("=".repeat(60));

        question1();
        question2a();
        question2b();
    }
}
