package net.nighthawkempires.koth.koth.registry;

import net.nighthawkempires.core.datasection.AbstractFileRegistry;
import net.nighthawkempires.koth.koth.KoTHModel;

import java.util.Map;

public class FKoTHRegistry extends AbstractFileRegistry<KoTHModel> implements KoTHRegistry {
    private static final boolean SAVE_PRETTY = true;

    public FKoTHRegistry() {
        super("empires", SAVE_PRETTY, -1);
    }

    public FKoTHRegistry(String path) {
        super(path, SAVE_PRETTY, -1);
    }

    @Override
    public Map<String, KoTHModel> getRegisteredData() {
        return REGISTERED_DATA.asMap();
    }
}
