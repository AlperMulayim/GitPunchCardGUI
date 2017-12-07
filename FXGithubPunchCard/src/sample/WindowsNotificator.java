package sample;
import java.awt.*;

/**
 * Created by Alper on 22.11.2017.
 */
public class WindowsNotificator {
    private String message;
    private Image image;
    private SystemTray tray;

    public WindowsNotificator() {
    }

    public void sendNotification(String header, String message) throws AWTException {
        tray  = SystemTray.getSystemTray();
        image  = Toolkit.getDefaultToolkit().createImage("icon.png");
        TrayIcon trayIcon = new TrayIcon( image,"Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);
        trayIcon.displayMessage(header, message, TrayIcon.MessageType.INFO);
        tray.remove(trayIcon);
    }

}
