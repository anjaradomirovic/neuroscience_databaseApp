// model/AlatGrupaDTO.java
package org.baze.neuronauka.model;

public class AlatGrupaDTO {

    private int    idTipAlata;
    private String tipAlata;
    private int    ukupnoAlata;

    public AlatGrupaDTO(int idTipAlata, String tipAlata, int ukupnoAlata) {
        this.idTipAlata  = idTipAlata;
        this.tipAlata    = tipAlata;
        this.ukupnoAlata = ukupnoAlata;
    }

    public int    getIdTipAlata()  { return idTipAlata; }
    public String getTipAlata()    { return tipAlata; }
    public int    getUkupnoAlata() { return ukupnoAlata; }

    @Override
    public String toString() {
        return tipAlata + " (" + ukupnoAlata + ")";
    }
}