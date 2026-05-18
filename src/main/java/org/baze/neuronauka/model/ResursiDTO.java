package org.baze.neuronauka.model;
import org.baze.neuronauka.model.TipAlataDTO;
import java.math.BigDecimal;

public class ResursiDTO {

    private int        idResurs;
    private String     naziv;
    private BigDecimal dostupnaKolicina;

    public ResursiDTO() {}

    public ResursiDTO(int idResurs, String naziv, BigDecimal dostupnaKolicina) {
        this.idResurs         = idResurs;
        this.naziv            = naziv;
        this.dostupnaKolicina = dostupnaKolicina;
    }

    public int        getIdResurs()                        { return idResurs; }
    public void       setIdResurs(int v)                   { this.idResurs = v; }

    public String     getNaziv()                           { return naziv; }
    public void       setNaziv(String v)                   { this.naziv = v; }

    public BigDecimal getDostupnaKolicina()                { return dostupnaKolicina; }
    public void       setDostupnaKolicina(BigDecimal v)    { this.dostupnaKolicina = v; }
}