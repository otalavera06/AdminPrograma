package Modeloak;

public class Faktura {
    private int id;
    private double prezio_totala;
    private int sortuta;
    private String path;
    private int zerbitzua_id;

    public Faktura() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getPrezio_totala() { return prezio_totala; }
    public void setPrezio_totala(double prezio_totala) { this.prezio_totala = prezio_totala; }

    public int getSortuta() { return sortuta; }
    public void setSortuta(int sortuta) { this.sortuta = sortuta; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public int getZerbitzua_id() { return zerbitzua_id; }
    public void setZerbitzua_id(int zerbitzua_id) { this.zerbitzua_id = zerbitzua_id; }
}
