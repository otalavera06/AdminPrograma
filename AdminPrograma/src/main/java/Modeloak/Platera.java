package Modeloak;

public class Platera {
    private int id;
    private String izena;
    private String mota;
    private double prezioa;
    private int platera_motak_id;

    public Platera() {}

    public Platera(int id, String izena, String mota, double prezioa, int platera_motak_id) {
        this.id = id;
        this.izena = izena;
        this.mota = mota;
        this.prezioa = prezioa;
        this.platera_motak_id = platera_motak_id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public String getMota() { return mota; }
    public void setMota(String mota) { this.mota = mota; }

    public double getPrezioa() { return prezioa; }
    public void setPrezioa(double prezioa) { this.prezioa = prezioa; }

    public int getPlatera_motak_id() { return platera_motak_id; }
    public void setPlatera_motak_id(int platera_motak_id) { this.platera_motak_id = platera_motak_id; }
}
