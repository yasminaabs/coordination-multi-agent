package models;

import java.util.ArrayList;
import java.util.List;

/** Préférences d'un agent acheteur (compagnies préférées/refusées, type de transport). */
public class PreferenceAcheteur {

    private final List<String> compagniesPreferees;
    private final List<String> compagniesRefusees;
    private final String       typeServicePrefere;   // "avion" ou "train"

    public PreferenceAcheteur(List<String> compagniesPreferees,
                               List<String> compagniesRefusees,
                               String typeServicePrefere) {
        this.compagniesPreferees = new ArrayList<>(compagniesPreferees);
        this.compagniesRefusees  = new ArrayList<>(compagniesRefusees);
        this.typeServicePrefere  = typeServicePrefere;
    }

    public List<String> getCompagniesPreferees()  { return compagniesPreferees; }
    public List<String> getCompagniesRefusees()   { return compagniesRefusees; }
    public String       getTypeServicePrefere()   { return typeServicePrefere; }
}
