import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;
import org.graalvm.polyglot.Engine;

import java.io.Closeable;

public class ValkyrieController implements Closeable {

    ValkyrieController(TruffleInstrument.Env env) {
        this.env = env;
    }

    private final TruffleInstrument.Env env;

    private SourceSectionFilter filter = null;

    public synchronized void setFilter(SourceSectionFilter filter) {
        this.filter = filter;
    }

    public static Valkyrie find(Engine engine) {
        return Valkyrie.getInstrument(engine);
    }

    public void close() {

    }
}
