package org.baze.neuronauka.model;

public class AlatStatusDTO {

    private int     idLaboratorija;
    private String  nazivLaboratorije;
    private String  tipAlata;
    private int     ukupnoAlata;
    private int     brojAktivnih;
    private int     brojNaServisu;
    private int     brojEksperimenata;

    // aktivan = true  → status "aktivan"
    // aktivan = false → status "na servisu"
    // Ovo se koristi za toggle dugme; ne čuva se direktno u bazi
    // (baza čuva po svakom alatu, VIEW agregira)
    private boolean aktivan;

    public AlatStatusDTO(int idLaboratorija, String nazivLaboratorije,
                         String tipAlata, int ukupnoAlata,
                         int brojAktivnih, int brojNaServisu,
                         int brojEksperimenata) {
        this.idLaboratorija    = idLaboratorija;
        this.nazivLaboratorije = nazivLaboratorije;
        this.tipAlata          = tipAlata;
        this.ukupnoAlata       = ukupnoAlata;
        this.brojAktivnih      = brojAktivnih;
        this.brojNaServisu     = brojNaServisu;
        this.brojEksperimenata = brojEksperimenata;
        // toggle je aktivan ako je bar jedan alat aktivan u tipu
        this.aktivan           = brojAktivnih > 0;
    }

    // ── Getteri / Setteri ─────────────────────────────────────────

    public int    getIdLaboratorija()    { return idLaboratorija; }
    public String getNazivLaboratorije() { return nazivLaboratorije; }
    public String getTipAlata()          { return tipAlata; }
    public int    getUkupnoAlata()       { return ukupnoAlata; }
    public int    getBrojAktivnih()      { return brojAktivnih; }
    public int    getBrojNaServisu()     { return brojNaServisu; }
    public int    getBrojEksperimenata() { return brojEksperimenata; }
    public boolean isAktivan()           { return aktivan; }

    public void setAktivan(boolean aktivan) {
        this.aktivan = aktivan;
    }
    public void setBrojAktivnih(int n)   { this.brojAktivnih  = n; }
    public void setBrojNaServisu(int n)  { this.brojNaServisu = n; }
}