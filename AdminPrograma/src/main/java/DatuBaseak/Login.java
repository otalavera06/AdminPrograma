package DatuBaseak;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Login extends MySql{
    private String erabiltzailea;
    private String pasahitza;


    public boolean loginaEgin(String erabiltzailea, String pasahitza){
        boolean baliozkoa = false;
        String sql = "select * from langileak where erabiltzailea = ? and pasahitza = ?";
        try(Connection conn = konektatu();
            PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setString(1, erabiltzailea);
            stm.setString(2, pasahitza);

            try (ResultSet rs = stm.executeQuery()){
                if (rs.next()){
                    baliozkoa = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return baliozkoa;
    }

    public Login() {
        super();
    }
}
