package InstrumentClasses;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.js.runtime.JSArguments;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Engine;

import java.util.Iterator;



@TruffleInstrument.Registration(id = "Valkyrie", name = "Valkyrie Instrument", services = {ProgramController.class})
public final class Valkyrie extends TruffleInstrument {

    public ProgramController controller;
    private Env env;


    private static ProfilerToolFactory<ProgramController> factory;

    public static void setFactory(ProfilerToolFactory<ProgramController> factory) {
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
        System.out.println("<<<<<<<<< Entered onCreate");
//        System.out.println(env.getInstrumenter().);
        SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
        SourceSectionFilter filter = builder.tagIs(StandardTags.CallTag.class).build();
        Instrumenter instrumenter = env.getInstrumenter();
//        instrumenter.attachExecutionEventListener(filter, new ExecutionEventListener() {
           // @Override
//            public void onEnter(EventContext context, VirtualFrame frame) {
//
//                try {
//                    System.out.println("Entered onEnter in ExecutionEventListener");
////                CompilerDirectives.transferToInterpreter();
////                // notify the runtime that we will change the current execution flow
////                throw context.createUnwind(null);
////                System.out.println(context);
//                    Node node = context.getInstrumentedNode();
//                    Iterator<Node> nodeIterator = node.getChildren().iterator();
//                    switch (nodeIterator.next().getSourceSection().getCharacters().toString()) {
//                        case "createAssets":
//                            nodeIterator.forEachRemaining(k -> System.out.println(k.getEncapsulatingSourceSection()));
//                            //controller.tryTransaction (call to static method in instance of controller class)
//                            throw context.createUnwind(true);
//                        default:
//                    }
//                }
//                catch (Exception e) {
//                    throw context.createUnwind(null);
//                }
//
//                System.out.println();
//
//            }
//
//           // @Override
//            public void onReturnValue(EventContext context, VirtualFrame frame, Object result) {
//
////                CompilerDirectives.transferToInterpreter();
////                // notify the runtime that we will change the current execution flow
////                throw context.createUnwind(null);
//
//            }
//
//            //@Override
//            public void onReturnExceptional(EventContext context, VirtualFrame frame, Throwable exception) {
////                System.out.println("Entered onReturnExceptional for ExecutionEventListener");
//
//            }
//
//            //@Override
//            public Object onUnwind(EventContext context, VirtualFrame frame, Object info) {
//                return info;
//            }
//        });
        instrumenter.attachExecutionEventFactory(filter, new ExecutionEventNodeFactory(){

            @Override
            public ExecutionEventNode create(EventContext context) {
//                System.out.println(context.getInstrumentedNode().getEncapsulatingSourceSection().getCharacters());
                return new ExecutionEventNode() {
                    @Override
                    protected void onEnter(VirtualFrame frame) {
                        try {
                            Node node = context.getInstrumentedNode();
                            Iterator<Node> nodeIterator = node.getChildren().iterator();
                            String methodName = nodeIterator.next().getSourceSection().getCharacters().toString();
                            System.out.println(methodName);
                            System.out.println("Frame arguments length \n " + frame.getArguments().length);

                            if (methodName.contains(".")) {
                                String separatedNames[] = methodName.split("\\.");
                                switch (separatedNames[0]) {
                                    case "ValkyrieReserved":
                                        switch(separatedNames[1]) {
                                            case "createAssets":
                                                try {
                                                    Object[] functionArguments = JSArguments.extractUserArguments(frame.getArguments());

                                                    if (functionArguments.length == 6) {
//                                                        try {
                                                            //TODO correct parsing of values, specifically Longs
                                                            String issuer = (String) functionArguments[0];
                                                            String to = (String) functionArguments[1];
                                                            Long amount = new Long((Integer) functionArguments[2]);
                                                            String assetCode = (String) functionArguments[3];
                                                            Long fee = new Long((Integer) functionArguments[4]);
                                                            String data = (String) functionArguments[5];
                                                            System.out.println(separatedNames[0] + "\n" + separatedNames[1] + "\n" + issuer + "\n" + to + "\n" + amount + "\n" + assetCode + "\n" + fee + "\n" + data);

                                                            //Make transaction using Controller
                                                            controller.createAssets(issuer, to, amount, assetCode, fee, data);

                                                            //TODO return tx json like API does
                                                            CompilerDirectives.transferToInterpreterAndInvalidate();
                                                            throw context.createUnwind(true);

//                                                        }
//
//                                                        catch (Exception e) {
//                                                            System.out.println(e.getMessage());
//                                                            CompilerDirectives.transferToInterpreterAndInvalidate();
//                                                            throw context.createUnwind("Error: Invalid arguments types");
//                                                        }
                                                    }

                                                    else {
                                                        CompilerDirectives.transferToInterpreterAndInvalidate();
                                                        throw context.createUnwind("Error: Incorrect number of arguments");
                                                    }
                                                }

                                                catch (Exception e) {
                                                    System.out.println(e);
                                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                                    throw context.createUnwind(e);
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

                        System.out.println();
                    }


                    @Override
                    public void onReturnValue(VirtualFrame frame, Object result) {
////                        try {
//                            System.out.println();
//                            System.out.println(result);
//
////                            System.out.println((String)result);
//
//
//                            System.out.println(isJSUserObject(result));
//
//                            DynamicObject obj = (DynamicObject) result;
//
//                            System.out.println(obj.get("to"));
//
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

//        this.controller = new ProgramController(env);
        controller = factory.create(env);
        System.out.println("Registering Controller");
        env.registerService(controller);
//        System.out.println(env.lookup(env.getInstruments().get("Valkyrie"), ProgramController.class));
        System.out.println("Registered Controller");
        System.out.println(controller.getNewAssetInstances());
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
        System.out.println("<<<<<<<<Entered onDispose");
        System.out.println(controller.getNewAssetInstances());
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new ValkyrieOptionOptionDescriptors();
    }

}

