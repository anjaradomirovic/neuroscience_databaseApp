package org.baze.neuronauka.model;

public class SesijaResursDTO {
    private final int    idResurs;
    private final String naziv;
    private       double kolicina;

    public SesijaResursDTO(int idResurs, String naziv, double kolicina) {
        this.idResurs = idResurs;
        this.naziv    = naziv;
        this.kolicina = kolicina;
    }

    public int    getIdResurs()  { return idResurs; }
    public String getNaziv()     { return naziv; }
    public double getKolicina()  { return kolicina; }
    public void   setKolicina(double k) { this.kolicina = k; }

    @Override
    public String toString() { return naziv; }
}