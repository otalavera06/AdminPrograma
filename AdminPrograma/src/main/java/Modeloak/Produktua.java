package Modeloak;

public class Produktua {
    private int id;
    private String izena;
    private double prezioa;
    private int stock;
    private int produktuen_motak;

    public Produktua(int id, String izena, double prezioa, int stock, int produktuen_motak) {
        this.id = id;
        this.izena = izena;
        this.prezioa = prezioa;
        this.stock = stock;
        this.produktuen_motak = produktuen_motak;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIzena(String izena) {
        this.izena = izena;
    }

    public void setPrezioa(double prezioa) {
        this.prezioa = prezioa;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setProduktuen_motak(int produktuen_motak) {
        this.produktuen_motak = produktuen_motak;
    }

    public int getId() {
        return id;
    }

    public String getIzena() {
        return izena;
    }

    public double getPrezioa() {
        return prezioa;
    }

    public int getStock() {
        return stock;
    }

    public int getProduktuen_motak() {
        return produktuen_motak;
    }
}
