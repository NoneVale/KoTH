package net.nighthawkempires.koth.koth.registry;

import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Registry;
import net.nighthawkempires.koth.koth.KoTHModel;

import java.util.Map;

public interface KoTHRegistry extends Registry<KoTHModel> {

    default KoTHModel fromDataSection(String key, DataSection data) {
        if (key.equalsIgnoreCase("koth"))
            return new KoTHModel(data);
        return null;
    }

    default KoTHModel getConfig() {
        return fromKey("koth").orElseGet(() -> register(new KoTHModel()));
    }

    @Deprecated
    Map<String, KoTHModel> getRegisteredData();

    default boolean configExists() {
        return fromKey("koth").isPresent();
    }
}