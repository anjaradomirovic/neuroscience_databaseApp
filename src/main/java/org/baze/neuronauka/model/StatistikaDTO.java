package org.baze.neuronauka.model;

public class StatistikaDTO {

    public static class EksperimentStatistika {
        private final int    idEksperiment;
        private final String naziv;
        private final int    ukupno;
        private final int    aktivna;
        private final int    uspesno;
        private final int    neuspesno;
        private final int    naCekanju;
        private final double procenatUspesnosti;

        public EksperimentStatistika(int idEksperiment, String naziv,
                                     int ukupno, int aktivna,
                                     int uspesno, int neuspesno,
                                     int naCekanju, double procenatUspesnosti) {
            this.idEksperiment      = idEksperiment;
            this.naziv              = naziv;
            this.ukupno             = ukupno;
            this.aktivna            = aktivna;
            this.uspesno            = uspesno;
            this.neuspesno          = neuspesno;
            this.naCekanju          = naCekanju;
            this.procenatUspesnosti = procenatUspesnosti;
        }

        public int    getIdEksperiment()       { return idEksperiment; }
        public String getNaziv()               { return naziv; }
        public int    getUkupno()              { return ukupno; }
        public int    getAktivna()             { return aktivna; }
        public int    getUspesno()             { return uspesno; }
        public int    getNeuspesno()           { return neuspesno; }
        public int    getNaCekanju()           { return naCekanju; }
        public double getProcenatUspesnosti()  { return procenatUspesnosti; }
    }

    public static class LaboratorijaStatistika {
        private final int    idLaboratorija;
        private final String naziv;
        private final int    zavrsenih;

        public LaboratorijaStatistika(int idLaboratorija, String naziv, int zavrsenih) {
            this.idLaboratorija = idLaboratorija;
            this.naziv          = naziv;
            this.zavrsenih      = zavrsenih;
        }

        public int    getIdLaboratorija() { return idLaboratorija; }
        public String getNaziv()          { return naziv; }
        public int    getZavrsenih()      { return zavrsenih; }
    }

    public static class SumarneKartice {
        private final int    ukupno;
        private final int    aktivna;
        private final int    zavrsena;
        private final double prosekUspesnosti;

        public SumarneKartice(int ukupno, int aktivna,
                              int zavrsena, double prosekUspesnosti) {
            this.ukupno           = ukupno;
            this.aktivna          = aktivna;
            this.zavrsena         = zavrsena;
            this.prosekUspesnosti = prosekUspesnosti;
        }

        public int    getUkupno()           { return ukupno; }
        public int    getAktivna()          { return aktivna; }
        public int    getZavrsena()         { return zavrsena; }
        public double getProsekUspesnosti() { return prosekUspesnosti; }
    }
}