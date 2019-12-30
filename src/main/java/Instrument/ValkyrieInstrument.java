package Instrument;

import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.instrumentation.TruffleInstrument.Registration;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.js.runtime.JSArguments;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Engine;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;
import org.graalvm.options.OptionValues;

import java.util.Iterator;

/**
 * The parser instrument for evaluating ValkyrieInstrument function calls within Topl's Bifrost programs. This instrument uses
 * the {@link Registration} annotation to automatically register the instrument as a service usable during JVM runtime.
 */
@Registration(id = "ValkyrieInstrument", name = "ValkyrieInstrument Instrument", version = "1.0.0", services = {ProgramController.class})
public final class ValkyrieInstrument extends TruffleInstrument {


    /**
     * Default option to enable the instrument's use when called from a {@link org.graalvm.polyglot.Context}, called
     * during {@link #onCreate(Env)}
     */
    @Option(name = "", help = "Enable ValkyrieInstrument Instrument (default: false)", category = OptionCategory.USER, stability = OptionStability.EXPERIMENTAL)
    static final OptionKey<Boolean> ENABLED = new OptionKey<>(false);

    public ProgramController controller;
    private Env env;


    private static ServiceFactory<ProgramController> factory;

    public static void setFactory(ServiceFactory<ProgramController> factory) {
//        if (factory == null || !factory.getClass().getName().startsWith("com.oracle.truffle.tools.profiler")) {
//            throw new IllegalArgumentException("Wrong factory: " + factory);
//        }
        ValkyrieInstrument.factory = factory;
    }

    //Static block loaded at initialization
    static {
        // Be sure that the factory is initialized:
        try {
            Class.forName(ProgramController.class.getName(), true, ProgramController.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Can not happen
            throw new AssertionError();
        }
    }

    /**
     * Default override to initialize an instrument within an environment.
     *
     * @param env The environment for the listener instrument, where all options and input/output are available to use
     *            from the same environment.
     */
    @Override
    protected void onCreate(final Env env) {
        final OptionValues options = env.getOptions();
        if(ENABLED.getValue(options)) {
            enable(env);
            env.registerService(this);
        }
    }

    private void enable(final Env env) {
        controller = factory.create(env);
        SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        SourceSectionFilter filter = builder.tagIs(StandardTags.CallTag.class).build();
        Instrumenter instrumenter = env.getInstrumenter();
        instrumenter.attachExecutionEventFactory(filter, new ValkyrieEventFactory(this, controller));
    }

    public static ProgramController getController(Engine engine){
        Instrument instrument = engine.getInstruments().get("ValkyrieInstrument");
        if (instrument == null) {
            throw new IllegalStateException("Instrument is not installed.");
        }
        return instrument.lookup(ProgramController.class);
    }

    @Override
    protected void onDispose(TruffleInstrument.Env env) {
        controller.clear();
        controller = null;
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new ValkyrieInstrumentOptionDescriptors();
    }

    private static Long castObjectToLong(Object obj) {
        if(obj instanceof Integer) {
            return new Long((Integer)obj);
        }
        else if (obj instanceof Double) {
            return ((Double)obj).longValue();
        }
        else {
            throw new IllegalArgumentException("Could not cast to Long");
        }
    }

}

