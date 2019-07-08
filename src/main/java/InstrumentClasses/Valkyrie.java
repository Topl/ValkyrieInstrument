package InstrumentClasses;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.JSArguments;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSFunctionData;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Engine;

import java.util.Iterator;



@TruffleInstrument.Registration(id = "Valkyrie", name = "Valkyrie Instrument", services = {ProgramController.class})
public final class Valkyrie extends TruffleInstrument {

    public ProgramController controller;
    private Env env;


    private static ServiceFactory<ProgramController> factory;

    public static void setFactory(ServiceFactory<ProgramController> factory) {
//        if (factory == null || !factory.getClass().getName().startsWith("com.oracle.truffle.tools.profiler")) {
//            throw new IllegalArgumentException("Wrong factory: " + factory);
//        }
        Valkyrie.factory = factory;
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


    @Override
    protected void onCreate(TruffleInstrument.Env env) {
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
//                            if (methodName.contains("_")) {
//                                String separatedNames[] = methodName.split("_");

                                switch (separatedNames[0]) {
                                    case "ValkyrieReserved":
                                        switch(separatedNames[1]) {
                                            case "createAssets": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 6) {
                                                        //TODO correct parsing of values, specifically Longs
                                                        String issuer = (String) functionArguments[0];
                                                        String to = (String) functionArguments[1];
                                                        Long amount = new Long((Integer) functionArguments[2]);
                                                        String assetCode = (String) functionArguments[3];
                                                        Long fee = new Long((Integer) functionArguments[4]);
                                                        String data = (String) functionArguments[5];
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + issuer + " " + to + " " + amount + " " + assetCode + " " + fee + " " + data);

                                                        //Make transaction using Controller
                                                        controller.createAssets(issuer, to, amount, assetCode, fee, data);

                                                        //TODO return tx json like API does
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("Error: Incorrect number of arguments to Valkyrie_createAssets");
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind(e);
                                                }
                                            }

                                            case "transferAssets": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 6) {
                                                        //TODO correct parsing of values, specifically Longs
                                                        String issuer = (String) functionArguments[0];
                                                        String from = (String) functionArguments[1];
                                                        String to = (String) functionArguments[2];
                                                        Long amount = new Long((Integer) functionArguments[3]);
                                                        String assetCode = (String) functionArguments[4];
                                                        Long fee = new Long((Integer) functionArguments[5]);
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + issuer + " " + from + " " + to + " " + amount + " " + assetCode + " " + fee);

                                                        //Make transaction using Controller
                                                        controller.transferAssets(issuer, from, to, amount, assetCode, fee);

                                                        //TODO return tx json like API does
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("Error: Incorrect number of arguments to Valkyrie_transferAssets");
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind(e);
                                                }
                                            }

                                            case "transferArbits": {
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 4) {
                                                        //TODO correct parsing of values, specifically Longs
                                                        String from = (String) functionArguments[0];
                                                        String to = (String) functionArguments[1];
                                                        Long amount = new Long((Integer) functionArguments[2]);
                                                        Long fee = new Long((Integer) functionArguments[3]);
                                                        System.out.println(separatedNames[0] + " " + separatedNames[1] + " " + from + " " + to + " " + amount + " " + fee);

                                                        //Make transaction using Controller
                                                        controller.transferArbits(from, to, amount, fee);

                                                        //TODO return tx json like API does
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind(true);

                                                    } else {
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("Error: Incorrect number of arguments to Valkyrie_transferArbits");
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind(e);
                                                }
                                            }

                                            default:
                                                CompilerDirectives.transferToInterpreterAndInvalidate();
                                                throw context.createUnwind("Error: Method not found in Valkyrie namespace");

                                        }
                                        //Non valkyrie (ordinary) function - do nothing
                                    default:
                                }
                            }
                        }
                        catch (Exception e) {
                            System.out.println(e);
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

        controller = factory.create(env);
        env.registerService(controller);
        this.env = env;
    }

    public static ProgramController getController(Engine engine){
        Instrument instrument = engine.getInstruments().get("Valkyrie");
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
        controller = null;
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new ValkyrieOptionOptionDescriptors();
    }

}

