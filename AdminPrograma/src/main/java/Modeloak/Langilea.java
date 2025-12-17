package Modeloak;

public class Langilea {
    private int id;
    private String izena;
    private String abizena;
    private String email;
    private String telefonoa;
    private boolean baimena;
    private int langileMotaId;
    private String erabiltzailea;
    private String pasahitza;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getAbizena() { return abizena; }
    public void setAbizena(String abizena) { this.abizena = abizena; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefonoa() { return telefonoa; }
    public void setTelefonoa(String telefonoa) { this.telefonoa = telefonoa; }

    public boolean isBaimena() { return baimena; }
    public void setBaimena(boolean baimena) { this.baimena = baimena; }

    public int getLangileMotaId() { return langileMotaId; }
    public void setLangileMotaId(int langileMotaId) { this.langileMotaId = langileMotaId; }

    public String getErabiltzailea() { return erabiltzailea; }
    public void setErabiltzailea(String erabiltzailea) { this.erabiltzailea = erabiltzailea; }

    public String getPasahitza() { return pasahitza; }
    public void setPasahitza(String pasahitza) { this.pasahitza = pasahitza; }
}
