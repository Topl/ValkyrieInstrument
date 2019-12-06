// CheckStyle: start generated
package InstrumentClasses;

import InstrumentClasses.Valkyrie;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Provider;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import java.util.Arrays;
import java.util.Collection;

@GeneratedBy(Valkyrie.class)
@Registration(id = "Valkyrie", name = "Valkyrie Instrument", version = "1.0.0")
public class ValkyrieProvider implements Provider {

    @Override
    public String getInstrumentClassName() {
        return "InstrumentClasses.Valkyrie";
    }

    @Override
    public TruffleInstrument create() {
        return new Valkyrie();
    }

    @Override
    public Collection<String> getServicesClassNames() {
        return Arrays.asList("InstrumentClasses.ProgramController");
    }

}
