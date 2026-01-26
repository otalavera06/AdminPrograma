package Modeloak;

public class Erreserba {
    private int id;
    private String data;
    private int mota;
    private int erabiltzaileak_id;
    private int mahaiak_id;

    public Erreserba() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public int getMota() { return mota; }
    public void setMota(int mota) { this.mota = mota; }

    public int getErabiltzaileak_id() { return erabiltzaileak_id; }
    public void setErabiltzaileak_id(int erabiltzaileak_id) { this.erabiltzaileak_id = erabiltzaileak_id; }

    public int getMahaiak_id() { return mahaiak_id; }
    public void setMahaiak_id(int mahaiak_id) { this.mahaiak_id = mahaiak_id; }
}
