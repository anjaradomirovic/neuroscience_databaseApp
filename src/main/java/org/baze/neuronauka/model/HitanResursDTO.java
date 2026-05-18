package org.baze.neuronauka.model;

import java.math.BigDecimal;

/**
 * Resursi koji nedostaju laboratoriji (Laboratorija_Cekanje).
 */
public class HitanResursDTO {

    private int        idResurs;
    private String     naziv;
    private BigDecimal kolicineFali;

    public HitanResursDTO() {}

    public HitanResursDTO(int idResurs, String naziv, BigDecimal kolicineFali) {
        this.idResurs     = idResurs;
        this.naziv        = naziv;
        this.kolicineFali = kolicineFali;
    }

    public int        getIdResurs()                    { return idResurs; }
    public void       setIdResurs(int v)               { this.idResurs = v; }

    public String     getNaziv()                       { return naziv; }
    public void       setNaziv(String v)               { this.naziv = v; }

    public BigDecimal getKolicineFali()                { return kolicineFali; }
    public void       setKolicineFali(BigDecimal v)    { this.kolicineFali = v; }
}