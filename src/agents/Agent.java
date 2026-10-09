package agents;

import models.Message;
import negotiation.Environnement;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Classe abstraite de base pour tous les agents du SMA.
 *
 * Chaque agent possède :
 *   - un identifiant unique et un nom
 *   - une boîte de réception (file FIFO de messages)
 *   - une référence à l'environnement (canal de communication)
 *
 * Le comportement propre à chaque type d'agent est défini dans les
 * sous-classes via traiterMessage() et demarrerNegociation().
 */
public abstract class Agent {

    protected final String        agentId;
    protected final String        nom;
    protected       Environnement env;

    private final Queue<Message> boiteReception = new LinkedList<>();

    protected Agent(String agentId, String nom) {
        this.agentId = agentId;
        this.nom     = nom;
    }

    // ---- Communication -------------------------------------------------

    public void connecterEnvironnement(Environnement env) {
        this.env = env;
    }

    /** Dépose un message dans l'environnement pour acheminement. */
    protected void envoyerMessage(Message message) {
        if (env != null) env.acheminerMessage(message);
    }

    /** L'environnement dépose un message dans la boîte de réception. */
    public void recevoirMessage(Message message) {
        boiteReception.add(message);
    }

    /** Traite tous les messages en attente (appelé à chaque cycle). */
    public void traiterMessages() {
        while (!boiteReception.isEmpty()) {
            traiterMessage(boiteReception.poll());
        }
    }

    // ---- Comportements à implémenter -----------------------------------

    /** Logique de traitement d'un message entrant. */
    protected abstract void traiterMessage(Message message);

    /** Initialise la participation de l'agent à la négociation. */
    public abstract void demarrerNegociation();

    // ---- Accesseurs ----------------------------------------------------

    public String getAgentId() { return agentId; }
    public String getNom()     { return nom; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(id=" + agentId + ", nom=" + nom + ")";
    }
}
