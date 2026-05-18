package org.baze.neuronauka.model;

import java.time.LocalDate;

public class IzvodjenjeDTO {

    private int       idIzvodjenje;
    private LocalDate datumIzvodjenja;
    private String    status;
    private String    eksperiment;   // naziv — za prikaz u dropdown-u
    private String    laboratorija;  // naziv — za prikaz u dropdown-u

    private String dizajner;
    private String teorijskiOkvir;
    // ── nova polja ────────────────────────────────────────────────
    private int    idEksperimenta;
    private int    idLaboratorija;
    private java.util.List<org.baze.neuronauka.model.IzvodjenjeDTO.IzvodjenjeResursStavka>   resurei   = new java.util.ArrayList<>();
    private java.util.List<org.baze.neuronauka.model.IzvodjenjeDTO.IzvodjenjeIzvodjacStavka> izvodjaci = new java.util.ArrayList<>();

    // ── postojeći getteri/setteri ─────────────────────────────────
    public int       getIdIzvodjenje()    { return idIzvodjenje; }
    public LocalDate getDatumIzvodjenja() { return datumIzvodjenja; }
    public String    getStatus()          { return status; }
    public String    getEksperiment()     { return eksperiment; }
    public String    getLaboratorija()    { return laboratorija; }

    public void setIdIzvodjenje(int v)          { this.idIzvodjenje    = v; }
    public void setDatumIzvodjenja(LocalDate v) { this.datumIzvodjenja = v; }
    public void setStatus(String v)             { this.status          = v; }
    public void setEksperiment(String v)        { this.eksperiment     = v; }
    public void setLaboratorija(String v)       { this.laboratorija    = v; }

    // ── novi getteri/setteri ──────────────────────────────────────
    public int    getIdEksperimenta()           { return idEksperimenta; }
    public void   setIdEksperimenta(int v)      { this.idEksperimenta = v; }
    public int    getIdLaboratorija()           { return idLaboratorija; }
    public void   setIdLaboratorija(int v)      { this.idLaboratorija = v; }

    // aliasi koje novi kod očekuje
    public String getNazivEksperimenta()        { return eksperiment; }
    public void   setNazivEksperimenta(String v){ this.eksperiment = v; }
    public String getNazivLaboratorije()        { return laboratorija; }
    public void   setNazivLaboratorije(String v){ this.laboratorija = v; }

    public java.util.List<IzvodjenjeResursStavka>   getResurei()                              { return resurei; }
    public void setResurei(java.util.List<IzvodjenjeResursStavka> v)                          { this.resurei = v; }
    public java.util.List<IzvodjenjeIzvodjacStavka> getIzvodjaci()                           { return izvodjaci; }
    public void setIzvodjaci(java.util.List<IzvodjenjeIzvodjacStavka> v)                     { this.izvodjaci = v; }

    public String getDizajner()             { return dizajner; }
    public void   setDizajner(String v)     { this.dizajner = v; }
    public String getTeorijskiOkvir()       { return teorijskiOkvir; }
    public void   setTeorijskiOkvir(String v){ this.teorijskiOkvir = v; }
    @Override
    public String toString() {
        return "#" + idIzvodjenje
                + "  " + eksperiment
                + "  /  " + laboratorija
                + "  (" + datumIzvodjenja + ",  " + status + ")";
    }

    // ── ugnježdene klase ──────────────────────────────────────────

    public static class IzvodjenjeResursStavka {
        private int    idResurs;
        private String naziv;
        private double kolicina;

        public IzvodjenjeResursStavka(int idResurs, String naziv, double kolicina) {
            this.idResurs = idResurs;
            this.naziv    = naziv;
            this.kolicina = kolicina;
        }
        public int    getIdResurs()  { return idResurs; }
        public String getNaziv()     { return naziv; }
        public double getKolicina()  { return kolicina; }
    }

    public static class IzvodjenjeIzvodjacStavka {
        private int    idIstrazivaca;
        private String imeIPrezime;
        private String opisUloge;
        private String putDoBeleski;

        public IzvodjenjeIzvodjacStavka(int id, String imeIPrezime,
                                        String opisUloge, String putDoBeleski) {
            this.idIstrazivaca = id;
            this.imeIPrezime   = imeIPrezime;
            this.opisUloge     = opisUloge;
            this.putDoBeleski  = putDoBeleski;
        }
        public int    getIdIstrazivaca() { return idIstrazivaca; }
        public String getImeIPrezime()   { return imeIPrezime; }
        public String getOpisUloge()     { return opisUloge; }
        public String getPutDoBeleski()  { return putDoBeleski; }
    }
}