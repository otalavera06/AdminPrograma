package Klaseak;

public class Produktuak {
    private int id;
    private String izena;
    private double prezioa;
    private int stock;
    private int produktu_motak;

    public Produktuak(int id, String izena, double prezioa, int stock, int produktu_motak) {
        this.id = id;
        this.izena = izena;
        this.prezioa = prezioa;
        this.stock = stock;
        this.produktu_motak = produktu_motak;
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

    public void setProduktu_motak(int produktu_motak) {
        this.produktu_motak = produktu_motak;
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

    public int getProduktu_motak() {
        return produktu_motak;
    }
}
