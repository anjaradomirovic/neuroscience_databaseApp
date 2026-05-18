package org.baze.neuronauka.model;

import java.util.ArrayList;
import java.util.List;

public class EksperimentDTO {

    private int    idEksperiment;
    private String naziv;
    private String opis;
    private String tipEksperimenta;
    private String metodIzvodjenja;
    private boolean etickiOdobren;

    // ── stavke koje se dodaju uz eksperiment ──────────────────────
    private List<EksperimentResursStavka> resursi = new ArrayList<>();
    private List<EksperimentAlatStavka>   alati   = new ArrayList<>();

    // ── constructors ──────────────────────────────────────────────

    public EksperimentDTO() {}

    public EksperimentDTO(int idEksperiment, String naziv, String opis,
                          String tipEksperimenta, String metodIzvodjenja,
                          boolean etickiOdobren) {
        this.idEksperiment   = idEksperiment;
        this.naziv           = naziv;
        this.opis            = opis;
        this.tipEksperimenta = tipEksperimenta;
        this.metodIzvodjenja = metodIzvodjenja;
        this.etickiOdobren   = etickiOdobren;
    }

    // ── getters / setters ─────────────────────────────────────────

    public int     getIdEksperiment()    { return idEksperiment; }
    public void    setIdEksperiment(int v) { this.idEksperiment = v; }

    public String  getNaziv()            { return naziv; }
    public void    setNaziv(String v)    { this.naziv = v; }

    public String  getOpis()             { return opis; }
    public void    setOpis(String v)     { this.opis = v; }

    public String  getTipEksperimenta()  { return tipEksperimenta; }
    public void    setTipEksperimenta(String v) { this.tipEksperimenta = v; }

    public String  getMetodIzvodjenja()  { return metodIzvodjenja; }
    public void    setMetodIzvodjenja(String v) { this.metodIzvodjenja = v; }

    public boolean isEtickyOdobren()     { return etickiOdobren; }
    public void    setEtickyOdobren(boolean v) { this.etickiOdobren = v; }

    public List<EksperimentResursStavka> getResurei() { return resursi; }
    public void setResurei(List<EksperimentResursStavka> v) { this.resursi = v; }

    public List<EksperimentAlatStavka>   getAlati()   { return alati; }
    public void setAlati(List<EksperimentAlatStavka> v) { this.alati = v; }

    // u EksperimentDTO.java dodaj polje i getter/setter
    private List<int[]> dizajneri = new ArrayList<>();

    public List<int[]> getDizajneri()           { return dizajneri; }
    public void setDizajneri(List<int[]> v)     { this.dizajneri = v; }
    private String dizajneriStr;
    private String teorijskiOkvirStr;

    public String getDizajneriStr()              { return dizajneriStr; }
    public void   setDizajneriStr(String v)      { this.dizajneriStr = v; }
    public String getTeorijskiOkvirStr()         { return teorijskiOkvirStr; }
    public void   setTeorijskiOkvirStr(String v) { this.teorijskiOkvirStr = v; }

    @Override
    public String toString() { return naziv != null ? naziv : "(bez naziva)"; }

    // ══════════════════════════════════════════════════════════════
    //  Ugnježdene stavke
    // ══════════════════════════════════════════════════════════════

    /** Jedna stavka resursa vezana za eksperiment (resurs_eksperiment). */
    public static class EksperimentResursStavka {
        private int    idResurs;
        private String naziv;
        private String jedinica;
        private double kolicina;

        public EksperimentResursStavka() {}
        public EksperimentResursStavka(int idResurs, String naziv,
                                       String jedinica, double kolicina) {
            this.idResurs = idResurs;
            this.naziv    = naziv;
            this.jedinica = jedinica;
            this.kolicina = kolicina;
        }

        public int    getIdResurs()   { return idResurs; }
        public void   setIdResurs(int v)   { this.idResurs = v; }
        public String getNaziv()      { return naziv; }
        public void   setNaziv(String v)   { this.naziv = v; }
        public String getJedinica()   { return jedinica; }
        public void   setJedinica(String v){ this.jedinica = v; }
        public double getKolicina()   { return kolicina; }
        public void   setKolicina(double v){ this.kolicina = v; }

        @Override public String toString() {
            return naziv + " (" + jedinica + ")";
        }
    }

    /** Jedna stavka alata vezana za eksperiment (alat_eksperiment). */
    public static class EksperimentAlatStavka {
        private int    idAlat;
        private String naziv;
        private int    kolicina;

        public EksperimentAlatStavka() {}
        public EksperimentAlatStavka(int idAlat, String naziv, int kolicina) {
            this.idAlat   = idAlat;
            this.naziv    = naziv;
            this.kolicina = kolicina;
        }

        public int    getIdAlat()    { return idAlat; }
        public void   setIdAlat(int v)    { this.idAlat = v; }
        public String getNaziv()     { return naziv; }
        public void   setNaziv(String v)  { this.naziv = v; }
        public int    getKolicina()  { return kolicina; }
        public void   setKolicina(int v)  { this.kolicina = v; }

        @Override public String toString() { return naziv; }
    }
}