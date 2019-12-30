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

        //TODO refactor into enable function to only add source filter when ENABLED option is true
        SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        SourceSectionFilter filter = builder.tagIs(StandardTags.CallTag.class).build();
        Instrumenter instrumenter = env.getInstrumenter();
        instrumenter.attachExecutionEventFactory(filter, new ExecutionEventNodeFactory(){

            @Override
            public ExecutionEventNode create(EventContext context) {
                return new ExecutionEventNode() {
                    @Override
                    protected void onEnter(VirtualFrame frame) {
                        try {
//                            Object arg1 = frame.getArguments()[0];
//                            Object arg2 = frame.getArguments()[1];
//                            System.out.println(((DynamicObject) arg2).getShape());
//                            System.out.println(((DynamicObject) arg2).get("functionData"));


//                            Object functionObj = JSArguments.getFunctionObject(frame.getArguments());
//                            String methodName = JSFunction.getCallTarget((DynamicObject) functionObj).toString();
                            Node node = context.getInstrumentedNode();
                            Iterator<Node> nodeIterator = node.getChildren().iterator();
                            String methodName = nodeIterator.next().getSourceSection().getCharacters().toString();

                            if (methodName.contains(".")) {
                                String separatedNames[] = methodName.split("\\.");
                                switch (separatedNames[0]) {
                                    case "ValkyrieReserved":
                                        switch (separatedNames[1]) {
                                            case "createAssets": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 6) {
                                                        //TODO test long truncation
                                                        String issuer = (String) functionArguments[0];
                                                        String to = (String) functionArguments[1];
                                                        Long amount = castObjectToLong(functionArguments[2]);
                                                        String assetCode = (String) functionArguments[3];
                                                        Long fee = castObjectToLong(functionArguments[4]);
                                                        String data = (String) functionArguments[5];
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + issuer + " " + to + " " + amount + " " + assetCode + " " + fee + " " + data);

                                                        controller.createAssets(issuer, to, amount, assetCode, fee, data);

                                                        //TODO return tx json like API does
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        controller.didExecuteCorrectly = false;
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("ValkyrieError: Incorrect number of arguments to Valkyrie_createAssets");
                                                    }
                                                } catch (Exception e) {
                                                    controller.didExecuteCorrectly = false;
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind("ValkyrieError: " + e.getMessage());
                                                }
                                            }

                                            case "transferAssets": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 6) {
                                                        String issuer = (String) functionArguments[0];
                                                        String from = (String) functionArguments[1];
                                                        String to = (String) functionArguments[2];
                                                        Long amount = castObjectToLong(functionArguments[3]);
                                                        String assetCode = (String) functionArguments[4];
                                                        Long fee = castObjectToLong(functionArguments[5]);
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + issuer + " " + from + " " + to + " " + amount + " " + assetCode + " " + fee);

                                                        controller.transferAssets(issuer, from, to, amount, assetCode, fee);

                                                        //TODO return more meaningful response than boolean
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        controller.didExecuteCorrectly = false;
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("ValkyrieError: Incorrect number of arguments to Valkyrie_transferAssets");
                                                    }
                                                } catch (Exception e) {
                                                    controller.didExecuteCorrectly = false;
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind("ValkyrieError: " + e.getMessage());
                                                }
                                            }

                                            case "transferArbits": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 4) {
                                                        String from = (String) functionArguments[0];
                                                        String to = (String) functionArguments[1];
                                                        Long amount = castObjectToLong(functionArguments[2]);
                                                        Long fee = castObjectToLong(functionArguments[3]);
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + from + " " + to + " " + amount + " " + fee);

                                                        controller.transferArbits(from, to, amount, fee);

                                                        //TODO return more meaningful response than boolean
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        controller.didExecuteCorrectly = false;
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("ValkyrieError: Incorrect number of arguments to Valkyrie_transferArbits");
                                                    }
                                                } catch (Exception e) {
                                                    controller.didExecuteCorrectly = false;
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind("ValkyrieError: " + e.getMessage());
                                                }
                                            }

                                            default:
                                                controller.didExecuteCorrectly = false;
                                                CompilerDirectives.transferToInterpreterAndInvalidate();
                                                throw context.createUnwind("ValkyrieError: Method not found in ValkyrieInstrument namespace");

                                        }
                                        //Non valkyrie (ordinary) function - do nothing
                                    default:
                                }
                            }
                        }
                        catch (Exception e) {
                            //do nothing
                        }
                    }


                    @Override
                    public void onReturnValue(VirtualFrame frame, Object result) {

                    }

                    @Override
                    public void onReturnExceptional(VirtualFrame frame, Throwable exception) {

                    }


                    @Override
                    public Object onUnwind(VirtualFrame frame, Object info) {
                        return info;
                    }

                };
            }
        });

        //controller = factory.create(env);
        //env.registerService(controller);
        //this.env = env;
    }

    private void enable(final Env env) {
        //TODO move SourceSection here
    }

    public static ProgramController getController(Engine engine){
        Instrument instrument = engine.getInstruments().get("ValkyrieInstrument");
        if (instrument == null) {
            throw new IllegalStateException("Instrument is not installed.");
        }
        return instrument.lookup(ProgramController.class);
    }

    public Env getEnv() {
        return this.env;
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

