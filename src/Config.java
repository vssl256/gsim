import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.cfg";
    private static final String FILE_NOT_FOUND = "Couldn't find config file! Default config loaded.";
    private static final String VALUE_NOT_FOUND = "\nОтсутствует значение для параметра %s в конфиге. Используется значение по умолчанию.";
    private static Properties properties;

    static {
        properties = new Properties();
        loadDefaults();
        loadConfig();
    }

    private static void loadConfig() {
        try {
            Path configPath = findConfig();
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
            }

        } catch (Exception e) {
            System.out.println(FILE_NOT_FOUND);
        }
    }

    private static Path findConfig() throws URISyntaxException {
        String appImagePath = System.getenv("APPIMAGE");
        if (appImagePath != null) {
            Path appImageDir = Paths.get(appImagePath).getParent();
            Path appImageConfig = appImageDir.resolve(CONFIG_FILE);
            if (Files.exists(appImageConfig)) return appImageConfig;
            System.out.println(FILE_NOT_FOUND);
        }

        Path jarDir = Paths.get(
            Config.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toURI()
        ).getParent();
        return jarDir.resolve(CONFIG_FILE);
    }

    private static void loadDefaults() {
        properties.setProperty("physics.G", "6.6743e-11");
        properties.setProperty("physics.dt", "2.5");

        properties.setProperty("controller.defScale", "1e-9");

        properties.setProperty("graphics.lineWidth", "1");
        properties.setProperty("graphics.lineOpacity", "0.4");
        properties.setProperty("graphics.maxPoints", "20000");
    }

    public static double getDouble(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.out.printf(VALUE_NOT_FOUND, key);
            return Double.parseDouble(properties.getProperty(key));
        }
        return Double.parseDouble(value);
    }

    public static int getInt(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.out.printf(VALUE_NOT_FOUND, key);
            return Integer.parseInt(properties.getProperty(key));
        }
        return Integer.parseInt(value);
    }
}
