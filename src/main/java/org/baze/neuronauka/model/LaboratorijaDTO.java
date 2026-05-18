package org.baze.neuronauka.model;

public class LaboratorijaDTO {

    private int    idLaboratorija;
    private String naziv;
    private String tipLaboratorije;
    private String opisLokacije;

    public LaboratorijaDTO() {}

    public LaboratorijaDTO(int idLaboratorija, String naziv) {
        this.idLaboratorija = idLaboratorija;
        this.naziv = naziv;
    }

    public LaboratorijaDTO(int idLaboratorija, String naziv,
                           String tipLaboratorije, String opisLokacije) {
        this.idLaboratorija  = idLaboratorija;
        this.naziv           = naziv;
        this.tipLaboratorije = tipLaboratorije;
        this.opisLokacije    = opisLokacije;
    }

    // ─── getteri / setteri ───────────────────────────────────────

    public int    getIdLaboratorija()               { return idLaboratorija; }
    public void   setIdLaboratorija(int id)         { this.idLaboratorija = id; }

    public String getNaziv()                        { return naziv; }
    public void   setNaziv(String naziv)            { this.naziv = naziv; }

    public String getTipLaboratorije()              { return tipLaboratorije; }
    public void   setTipLaboratorije(String tip)    { this.tipLaboratorije = tip; }

    public String getOpisLokacije()                 { return opisLokacije; }
    public void   setOpisLokacije(String opis)      { this.opisLokacije = opis; }

    // Korisno za debug i eventualni prikaz u ListView direktno
    @Override
    public String toString() {
        return naziv;
    }
}