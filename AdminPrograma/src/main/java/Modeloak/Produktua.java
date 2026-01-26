package Modeloak;

public class Produktua {
    private int id;
    private String izena;
    private double prezioa;
    private int stock;
    private String irudia_path;
    private int produktuen_motak_id;
    private int stock_min;
    private int stock_max;

    public Produktua() {}

    public Produktua(int id, String izena, double prezioa, int stock, String irudia_path, int produktuen_motak_id, int stock_min, int stock_max) {
        this.id = id;
        this.izena = izena;
        this.prezioa = prezioa;
        this.stock = stock;
        this.irudia_path = irudia_path;
        this.produktuen_motak_id = produktuen_motak_id;
        this.stock_min = stock_min;
        this.stock_max = stock_max;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public double getPrezioa() { return prezioa; }
    public void setPrezioa(double prezioa) { this.prezioa = prezioa; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getIrudia_path() { return irudia_path; }
    public void setIrudia_path(String irudia_path) { this.irudia_path = irudia_path; }

    public int getProduktuen_motak_id() { return produktuen_motak_id; }
    public void setProduktuen_motak_id(int produktuen_motak_id) { this.produktuen_motak_id = produktuen_motak_id; }

    public int getStock_min() { return stock_min; }
    public void setStock_min(int stock_min) { this.stock_min = stock_min; }

    public int getStock_max() { return stock_max; }
    public void setStock_max(int stock_max) { this.stock_max = stock_max; }
}
