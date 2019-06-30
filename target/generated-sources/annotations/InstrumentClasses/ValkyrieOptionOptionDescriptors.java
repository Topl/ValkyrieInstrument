// CheckStyle: start generated
package InstrumentClasses;

import InstrumentClasses.ValkyrieOption;
import com.oracle.truffle.api.dsl.GeneratedBy;
import java.util.Arrays;
import java.util.Iterator;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionStability;

@GeneratedBy(ValkyrieOption.class)
final class ValkyrieOptionOptionDescriptors implements OptionDescriptors {

    @Override
    public OptionDescriptor get(String optionName) {
        switch (optionName) {
            case "Valkyrie" :
                return OptionDescriptor.newBuilder(ValkyrieOption.ENABLED, "Valkyrie").deprecated(false).help("Enable the Valkyrie instrument (default: false).").category(OptionCategory.USER).stability(OptionStability.STABLE).build();
        }
        return null;
    }

    @Override
    public Iterator<OptionDescriptor> iterator() {
        return Arrays.asList(
            OptionDescriptor.newBuilder(ValkyrieOption.ENABLED, "Valkyrie").deprecated(false).help("Enable the Valkyrie instrument (default: false).").category(OptionCategory.USER).stability(OptionStability.STABLE).build())
        .iterator();
    }

}
