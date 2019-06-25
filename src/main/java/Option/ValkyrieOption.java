package Option;

import com.oracle.truffle.api.Option;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;

@Option.Group("Valkyrie")
public class ValkyrieOption {

    @Option(name = "", help = "Enable the CPU tracer (default: false).", category = OptionCategory.USER, stability = OptionStability.STABLE) //
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

    @Option(name = "Valkyrie", help = "Enable the CPU tracer (default: false).", category = OptionCategory.USER, stability = OptionStability.STABLE) //
    static final OptionKey<Boolean> RANDOM = new OptionKey<>(false);
}
