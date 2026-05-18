package org.baze.neuronauka.model;

public class TipAlataDTO {
    private final int    idTipAlata;
    private final String naziv;

    public TipAlataDTO(int idTipAlata, String naziv) {
        this.idTipAlata = idTipAlata;
        this.naziv      = naziv;
    }

    public int    getIdTipAlata() { return idTipAlata; }
    public String getNaziv()      { return naziv; }

    @Override
    public String toString() { return naziv; }

}