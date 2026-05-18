package org.baze.neuronauka.controller;

import org.baze.neuronauka.dao.StatistikaDAO;
import org.baze.neuronauka.view.StatistikaContent;

public class StatistikaController {

    private final StatistikaContent content;
    private final StatistikaDAO     dao = new StatistikaDAO();

    public StatistikaController(StatistikaContent content) {
        this.content = content;
        content.setOnOsvezi(this::ucitaj);
        ucitaj();
    }

    private void ucitaj() {
        try {
            content.prikaziKartice(dao.getSumarneKartice());
            content.prikaziEksperimente(dao.getStatistikaPoEksperimentu());
            content.prikaziLaboratorije(dao.getStatistikaPoLaboratoriji());
        } catch (RuntimeException e) {
            // tiha greška — prikaži prazne tabele, ne ruši aplikaciju
            System.err.println("Greška pri učitavanju statistike: " + e.getMessage());
        }
    }
}