package mod.capitalfilter;

import script.SCRIPT;
import util.info.INFO;

public final class CapitalFilterScript implements SCRIPT {

    private final INFO info = new INFO("Capital Filter",
            "Filter settlement candidates by resources, adjacency, and climate.");

    public CapitalFilterScript() {
    }

    @Override
    public CharSequence name() {
        return info.name;
    }

    @Override
    public CharSequence desc() {
        return info.desc;
    }

    @Override
    public boolean forceInit() {
        return true;
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new CapitalFilterInstance();
    }
}
