# TP1 — Coordination multi-agent : négociation automatique de voyages

Ce projet met en œuvre un système multi-agent (SMA) de réservation de voyages. Des agents fournisseurs (compagnies aériennes et ferroviaires) et des agents acheteurs (voyageurs) négocient automatiquement l'achat de billets d'avion et de train, puis forment des coalitions pour maximiser la valeur globale du système.

Réalisé dans le cadre du TP1 de **Techniques de l'intelligence artificielle (TIA)**, en **Master 1 Informatique** à l'**Université Claude Bernard Lyon 1** (année universitaire 2025–2026).

**Auteurs :** Yasmina Abbas et Maria Sara Gherzouli.

Le [rapport du projet](./Rapport.pdf) détaille la modélisation, les fonctions d'utilité, les algorithmes et l'analyse des résultats.

## Fonctionnalités

Le programme traite les trois questions du TP :

- **Q1 — Négociation bilatérale** : un fournisseur et un acheteur échangent des offres, contre-offres, acceptations et refus. Les messages transitent par un environnement partagé (`Environnement`), qui les distribue en tours synchrones.
- **Q2a — Couplage (mode compétitif)** : formation de coalitions bilatérales fournisseur–acheteur, sans partage d'information.
- **Q2b — IDP et IP (mode coopératif)** : recherche de la structure de coalitions optimale, par programmation dynamique (IDP) et par partitions entières avec bornes supérieures, élagage et propriété *anytime* (IP), puis comparaison des deux algorithmes.

Le déroulement des négociations, les bilans et les statistiques s'affichent dans la console.

## Faire fonctionner le projet

### 1. Installer Java

Il faut un **JDK 17 ou plus récent** : le rapport utilise Java 17, et le code ne compile pas avant Java 16. Un JRE seul ne suffit pas, car il ne contient pas le compilateur `javac`. Si besoin, installez par exemple [Eclipse Temurin](https://adoptium.net/) en laissant cochée l'option qui ajoute Java au `PATH`.

Vérifiez ensuite, dans un nouveau terminal, que Java et le compilateur sont accessibles :

```powershell
java -version
javac -version
```

Les deux commandes doivent afficher une version 17 ou supérieure.

### 2. Récupérer le code

Clonez le dépôt (bouton **Code** sur GitHub) ou téléchargez-le en ZIP et décompressez-le. Ouvrez ensuite un terminal à la racine du projet, c'est-à-dire dans le dossier qui contient `README.md` et `src/`.

### 3. Compiler et lancer

**Windows (PowerShell)** :

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out Main
```

**Linux et macOS** :

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -type f -name '*.java')
java -cp out Main
```

La première commande crée le dossier `out/`. La deuxième compile tous les fichiers `.java` de `src/` et place les fichiers `.class` dans `out/`, sans modifier les sources. La troisième lance le programme à partir de la classe `Main`. Après une modification du code, relancez les deux dernières commandes.

### Avec VS Code

1. Installez l'extension **Extension Pack for Java** (Microsoft) et un JDK (étape 1).
2. Ouvrez le dossier du projet avec **Fichier → Ouvrir le dossier**.
3. Ouvrez `src/Main.java` et cliquez sur **Run**, au-dessus de la méthode `main`.

La sortie s'affiche dans le terminal intégré.

### Résultat attendu

Le programme ne demande aucune saisie. Il exécute Q1, Q2a et Q2b à la suite, affiche environ 165 lignes et se termine par la comparaison d'IDP et d'IP :

```text
============================================================
Algo             Valeur     Evaluations
----------------------------------------
IDP              952,27              15
IP               952,27              15
============================================================
Meme valeur optimale ? Oui

-- Analyse --
IDP et IP ont une efficacite similaire sur ce petit exemple.
```

Le séparateur décimal (virgule ou point) dépend de la langue du système.

### En cas de problème

- **« javac n'est pas reconnu » ou `javac: command not found`** : le JDK n'est pas installé ou n'est pas dans le `PATH`. Installez un JDK, puis fermez et rouvrez le terminal.
- **Erreur de compilation sur `toList()`** : la version de Java est trop ancienne. Installez un JDK 17 ou plus récent.
- **« Impossible de trouver ou de charger la classe principale Main »** : lancez la commande depuis la racine du projet, après la compilation, en gardant l'option `-cp out`.
- **Tirets affichés `?` ou `�` dans les titres** : la console n'utilise pas l'encodage UTF-8. Cela ne change pas les résultats. Sous Windows, avec Java 19 ou plus récent, lancez plutôt :

  ```powershell
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  java "-Dstdout.encoding=UTF-8" -cp out Main
  ```

## Modélisation

Chaque agent possède :

- des **contraintes dures** : budget maximum ou prix minimum, trajet, date limite et, pour l'acheteur, compagnies refusées. Si une contrainte est violée, l'utilité vaut −∞ ;
- des **préférences douces** : compagnies préférées, mode de transport préféré, date de vente souhaitée. Elles modifient l'utilité sans bloquer la transaction.

Lorsque les contraintes sont respectées, les utilités sont calculées ainsi (section 3 du rapport) :

| Fonction | Calcul |
|---|---|
| Utilité du fournisseur | `prix − prix_base`, multipliée par 1,10 si la vente a lieu avant la date souhaitée |
| Utilité de l'acheteur | `budget_max − prix`, multipliée par 1,20 pour une compagnie préférée et par 1,10 pour le mode de transport préféré |
| Utilité jointe (couplage) | `uF + uA`, évaluée au prix médian `(prix_min + budget_max) / 2` |
| Valeur d'une coalition mixte (IDP/IP) | somme des utilités jointes positives de ses paires fournisseur–acheteur, sur chaque service du fournisseur, multipliée par `1 + 0,05 × (n − 2)`, où *n* est le nombre de membres (bonus coopératif de 5 % par membre au-delà de 2) |

La stratégie de négociation limite chaque négociation à **6 propositions** au plus. Le prix varie au plus de **10 % par itération** : à l'itération *t*, le taux de variation vaut `min(10 %, 3 % × t)`. Le fournisseur baisse son prix et l'acheteur augmente le sien.

## Résultats de l'exemple

Le rapport présente les résultats ci-dessous ; le programme les reproduit à l'identique. Les dates des billets sont calculées à partir de la date d'exécution et diffèrent donc de celles du rapport, sans changer les valeurs.

### Q1 — Négociation bilatérale

3 transactions sont conclues sur 4, chacune après une seule contre-offre (2 tours), pour un total de **790,00 EUR**.

| Fournisseur | Acheteur | Service | Prix initial | Prix final |
|---|---|---|---|---|
| AirFrance | Alice | VOL-001 | 550,00 EUR | 480,00 EUR |
| SNCF | Carole | TRAIN-01 | 80,00 EUR | 70,00 EUR |
| Iberia | David | VOL-003 | 280,00 EUR | 240,00 EUR |

Delta ne vend pas son billet VOL-002 et Bob ne trouve pas de fournisseur compatible pendant cette phase. Le réseau échange 9 messages : 3 offres, 3 contre-offres et 3 acceptations, sans aucun refus.

### Q2a — Couplage compétitif

4 coalitions bilatérales sont formées en 4 itérations et tous les agents sont appariés. La valeur totale de la structure est de **522,50**.

| Itération | Coalition | Service | Valeur jointe |
|---|---|---|---|
| 1 | AirFrance ↔ Alice | VOL-001 | 244,20 |
| 2 | Delta ↔ Bob | VOL-002 | 124,30 |
| 3 | SNCF ↔ Carole | TRAIN-01 | 48,00 |
| 4 | Iberia ↔ David | VOL-003 | 106,00 |

### Q2b — IDP et IP coopératifs

Les deux algorithmes sont exécutés sur un sous-ensemble de 4 agents : AirFrance et Delta (fournisseurs), Alice et Bob (acheteurs). Ils trouvent la même structure optimale, la grande coalition {AirFrance, Delta, Alice, Bob}, de valeur **952,27**, avec **15 évaluations** de la fonction de valeur chacun.

IP n'explore que le sous-espace [4] et élague les 4 autres ([2, 2], [3, 1], [2, 1, 1] et [1, 1, 1, 1]) grâce à ses bornes supérieures. Sur cet exemple, IDP et IP font le même nombre d'évaluations, car la grande coalition obtient la borne la plus élevée ; l'avantage de l'élagage d'IP apparaît avec un plus grand nombre d'agents.

### Comparaison

| Critère | Couplage | IDP | IP |
|---|---|---|---|
| Taille des coalitions | 2 seulement | Quelconque | Quelconque |
| Mode | Compétitif | Coopératif | Coopératif |
| Optimalité | Non garantie | Garantie | Garantie |
| Propriété *anytime* | Non | Non | Oui |
| Complexité | O(n²) | O(3ⁿ) | ≤ O(3ⁿ) |
| Évaluations (ici) | — | 15 | 15 |
| Partage d'information | Non | Oui (+5 %) | Oui (+5 %) |
| Valeur obtenue | 522,50 | 952,27 | 952,27 |

Le rapport attribue la valeur presque doublée du mode coopératif au bonus coopératif et à l'optimisation globale de la structure. Ces valeurs sont des sommes d'utilités, et non des montants de ventes. De plus, le couplage porte sur les 8 agents du scénario et IDP/IP sur 4 agents seulement : la comparaison est donc indicative.

## Déroulement de l'exécution

Le point d'entrée `Main` construit un scénario avec 4 fournisseurs (AirFrance, Delta, SNCF, Iberia), 4 acheteurs (Alice, Bob, Carole, David) et 5 services : trois vols Paris → New York, un train Paris → Lyon et un vol Lyon → Barcelone. Il exécute ensuite les trois questions dans l'ordre :

1. **Q1** — chaque fournisseur propose ses services aux acheteurs compatibles ; les agents échangent leurs messages, puis le programme affiche le bilan des ventes, des achats et des messages échangés.
2. **Q2a** — l'algorithme de couplage forme des paires fournisseur–acheteur et affiche les coalitions obtenues.
3. **Q2b** — IDP et IP sont exécutés sur le sous-ensemble AirFrance, Delta, Alice et Bob, puis comparés (valeur optimale et nombre d'évaluations).

Chaque question repart d'un scénario neuf. Les agents, contraintes, préférences et stratégies sont définis dans `creerScenario()` dans `src/Main.java` ; vous pouvez les modifier pour tester d'autres situations.

## Organisation du code

```text
src/
├── Main.java                        # Point d'entrée : scénario et questions Q1, Q2a, Q2b
├── agents/
│   ├── Agent.java                   # Classe abstraite de base
│   ├── AgentFournisseur.java        # Agent vendeur
│   └── AgentAcheteur.java           # Agent acheteur
├── models/
│   ├── Service.java                 # Billet de voyage
│   ├── Message.java                 # Unité de communication
│   ├── TypeMessage.java             # Types de messages (OFFRE, CONTRE_OFFRE, ...)
│   ├── ContrainteAcheteur.java      # Contraintes dures de l'acheteur
│   ├── ContrainteFournisseur.java   # Contraintes dures du fournisseur
│   ├── PreferenceAcheteur.java      # Préférences douces de l'acheteur
│   └── PreferenceFournisseur.java   # Préférences douces du fournisseur
├── negotiation/
│   ├── Environnement.java           # Broker de messages
│   └── StrategieNegociation.java    # Paramètres de stratégie
└── coalitions/
    ├── AlgorithmeCouplage.java      # Q2a : mode compétitif
    ├── AlgorithmeIDP.java           # Q2b : programmation dynamique
    └── AlgorithmeIP.java            # Q2b : partitions entières (anytime)
```

## Limites et contexte

Ce dépôt est un projet pédagogique de simulation : le scénario est défini dans le code et l'exécution se fait en mémoire, sans interface graphique, base de données ni service externe. Les résultats dépendent des paramètres du scénario.

La recherche de structures de coalitions a une complexité exponentielle ; l'exemple fourni est volontairement de petite taille et ne constitue pas une évaluation de performance à grande échelle.
