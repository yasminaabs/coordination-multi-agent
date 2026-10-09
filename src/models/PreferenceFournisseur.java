package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Préférences d'un agent fournisseur. */
public class PreferenceFournisseur {

    private LocalDate    dateSouhaiteeVente;   // date de mise en vente idéale
    private List<String> acheteursПreferes;     // IDs d'acheteurs préférés

    public PreferenceFournisseur() {
        this.acheteursПreferes = new ArrayList<>();
    }

    public PreferenceFournisseur(LocalDate dateSouhaiteeVente) {
        this.dateSouhaiteeVente = dateSouhaiteeVente;
        this.acheteursПreferes  = new ArrayList<>();
    }

    public LocalDate    getDateSouhaiteeVente()  { return dateSouhaiteeVente; }
    public List<String> getAcheteursPreferes()   { return acheteursПreferes; }
}
