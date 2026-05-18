// model/AlatDetaljiDTO.java
package org.baze.neuronauka.model;

public class AlatDetaljiDTO {

    private int    idAlat;
    private String tipAlata;
    private String statusAlata;   // "aktivan" ili "na servisu"

    public AlatDetaljiDTO(int idAlat, String tipAlata, String statusAlata) {
        this.idAlat     = idAlat;
        this.tipAlata   = tipAlata;
        this.statusAlata = statusAlata;
    }

    public int    getIdAlat()      { return idAlat; }
    public String getTipAlata()    { return tipAlata; }
    public String getStatusAlata() { return statusAlata; }

    public boolean isAktivan() {
        return "aktivan".equalsIgnoreCase(statusAlata);
    }

    public void setStatusAlata(String statusAlata) {
        this.statusAlata = statusAlata;
    }
}