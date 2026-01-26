package Modeloak;

public class Erabiltzailea {
    private int id;
    private String izena;
    private String email;
    private String pasahitza;
    private String telefonoa;
    private String abizena;

    public Erabiltzailea() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasahitza() { return pasahitza; }
    public void setPasahitza(String pasahitza) { this.pasahitza = pasahitza; }

    public String getTelefonoa() { return telefonoa; }
    public void setTelefonoa(String telefonoa) { this.telefonoa = telefonoa; }

    public String getAbizena() { return abizena; }
    public void setAbizena(String abizena) { this.abizena = abizena; }
}
