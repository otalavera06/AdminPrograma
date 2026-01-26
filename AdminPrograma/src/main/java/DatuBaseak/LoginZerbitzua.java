package DatuBaseak;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Erabiltzailearen login-a API bidez egiten duen klasea.
 * EZ du MySQL-era konektatzen; C# API1-era deitzen du zuzenean.
 */
public class LoginZerbitzua {

    // API1-eko oinarrizko URLa (zure C# API-a)
    private static final String API_BASE_URL = "http://localhost:5005/api";

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Login egiten du API-ko /Langilea/login endpoint-era deituz.
     *
     * @param erabiltzailea erabiltzaile-izena
     * @param pasahitza     pasahitza
     * @return true  → login zuzena (200 OK)
     *         false → erabiltzailea/pasahitza oker edo API errorea
     */
    public boolean loginaEgin(String erabiltzailea, String pasahitza) {
        try {
            // API1-eko LoginDto-k "Erabiltzailea" eta "Pasahitza" jasotzen ditu
            String json = String.format(
                    "{\"Erabiltzailea\":\"%s\",\"Pasahitza\":\"%s\"}",
                    escape(erabiltzailea),
                    escape(pasahitza)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Langilea/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int kodea = response.statusCode();

            // 200 OK  → login ondo
            // 401     → erabiltzailea/pasahitza oker
            return kodea == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // JSON-en komatxoak eta barra bikoitza ihes egiteko
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
