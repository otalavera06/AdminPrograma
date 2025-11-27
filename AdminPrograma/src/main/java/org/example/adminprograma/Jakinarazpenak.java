package org.example.adminprograma;

import java.awt.*;

public class Jakinarazpenak implements Mensajeak {
    private TrayIcon trayIcon;

    public Jakinarazpenak(){
        if (SystemTray.isSupported()){
            SystemTray bandeja = SystemTray.getSystemTray();
            Image jakinarazpenIrudia = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            trayIcon = new TrayIcon(jakinarazpenIrudia);
            trayIcon.setImageAutoSize(true);

            try {
                bandeja.add(trayIcon);
            }catch (AWTException e){
                System.out.println(e.getMessage());
            }
        }
    }
    @Override
    public void erakutsi(String izenburua, String mezua, TrayIcon.MessageType tipoa){
        if(trayIcon != null){
            trayIcon.displayMessage(izenburua, mezua, tipoa);
        }
    }
}
