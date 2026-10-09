package negotiation;

import agents.Agent;
import agents.AgentAcheteur;
import agents.AgentFournisseur;
import models.Message;

import java.util.*;

/**
 * Environnement partagé du SMA.
 *
 * Joue le rôle de canal de communication : les agents s'y enregistrent,
 * y déposent leurs messages, et l'environnement les achemine aux destinataires.
 * La méthode executerCycle() déclenche le traitement synchrone de tous les
 * messages en attente dans toutes les boîtes de réception.
 */
public class Environnement {

    private final Map<String, Agent> agents  = new LinkedHashMap<>();
    private final List<Message>      journal = new ArrayList<>();

    // ---- Enregistrement ------------------------------------------------

    public void enregistrerAgent(Agent agent) {
        agents.put(agent.getAgentId(), agent);
        agent.connecterEnvironnement(this);
    }

    // ---- Communication -------------------------------------------------

    public void acheminerMessage(Message message) {
        journal.add(message);
        Agent dest = agents.get(message.getDestinatireId());
        if (dest != null) dest.recevoirMessage(message);
    }

    // ---- Exécution -----------------------------------------------------

    /** Un cycle : chaque agent traite l'ensemble de ses messages en attente. */
    public void executerCycle() {
        for (Agent agent : new ArrayList<>(agents.values())) {
            agent.traiterMessages();
        }
    }

    // ---- Accès par type ------------------------------------------------

    public List<AgentFournisseur> getFournisseurs() {
        List<AgentFournisseur> res = new ArrayList<>();
        for (Agent a : agents.values())
            if (a instanceof AgentFournisseur) res.add((AgentFournisseur) a);
        return res;
    }

    public List<AgentAcheteur> getAcheteurs() {
        List<AgentAcheteur> res = new ArrayList<>();
        for (Agent a : agents.values())
            if (a instanceof AgentAcheteur) res.add((AgentAcheteur) a);
        return res;
    }

    // ---- Statistiques --------------------------------------------------

    public void afficherStatistiques() {
        long offres    = journal.stream().filter(m -> m.getType().name().equals("OFFRE")).count();
        long accepts   = journal.stream().filter(m -> m.getType().name().equals("ACCEPTATION")).count();
        long refus     = journal.stream().filter(m -> m.getType().name().equals("REFUS")).count();
        long contres   = journal.stream().filter(m -> m.getType().name().equals("CONTRE_OFFRE")).count();
        System.out.printf("Statistiques reseau : total=%d | offres=%d | acceptations=%d | refus=%d | contre-offres=%d%n",
                journal.size(), offres, accepts, refus, contres);
    }
}
