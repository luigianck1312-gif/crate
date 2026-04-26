package it.cratePlugin.models;

public enum CrateType {
    COMUNE("comune"),
    NON_COMUNE("non_comune"),
    RARO("raro"),
    EPICO("epico"),
    LEGGENDARIO("leggendario"),
    MITICO("mitico");

    private final String configKey;

    CrateType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() { return configKey; }

    public static CrateType fromString(String s) {
        for (CrateType t : values()) {
            if (t.configKey.equalsIgnoreCase(s) || t.name().equalsIgnoreCase(s)) return t;
        }
        return null;
    }
}
