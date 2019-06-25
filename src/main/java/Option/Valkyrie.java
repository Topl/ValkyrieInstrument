package Option;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.*;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Engine;


@TruffleInstrument.Registration(id = "Valkyrie", services = Valkyrie.class)
public final class Valkyrie extends TruffleInstrument {

    private ValkyrieController controller;
    private Env env;
    @Override
    protected void onCreate(TruffleInstrument.Env env) {
        SourceSectionFilter.Builder builder = SourceSectionFilter.newBuilder();
//        SourceSectionFilter filter = builder.tagIs(StandardTags.CallTag.class).build();
        SourceSectionFilter filter = builder.tagIs(StandardTags.StatementTag.class).build();
//        SourceSectionFilter filter = builder.tagIs(JSTypes.class).build();
//        SourceSectionFilter.newBuilder()
//                .tagIs(StandardTags.StatementTag.class)
//                .mimeTypeIs("x-application/js")
//                .build();

//        SourceSectionFilter filter = builder
//                .tagIs(StandardTags.class)
//                .mimeTypeIs("x-application/js")
//                .build();


        Instrumenter instrumenter = env.getInstrumenter();
//        instrumenter.attachExecutionEventListener(filter, new ValkyrieInstrumentListener());

        instrumenter.attachExecutionEventListener(filter, new ExecutionEventListener() {
           // @Override
            public void onEnter(EventContext context, VirtualFrame frame) {
//                System.out.println("Entered onEnter in ExecutionEventListener");
                CompilerDirectives.transferToInterpreter();
                // notify the runtime that we will change the current execution flow
                throw context.createUnwind(null);
            }

           // @Override
            public void onReturnValue(EventContext context, VirtualFrame frame, Object result) {
//                System.out.println("Entered onReturnValue in ExecutionEventListener");
                CompilerDirectives.transferToInterpreter();
                // notify the runtime that we will change the current execution flow
                throw context.createUnwind(null);

            }

            //@Override
            public void onReturnExceptional(EventContext context, VirtualFrame frame, Throwable exception) {
//                System.out.println("Entered onReturnExceptional for ExecutionEventListener");

            }

//            //@Override
//            public Object onUnwind(EventContext context, VirtualFrame frame, Object info) {
//                return 42;
//            }
        });

        controller = new ValkyrieController(env);
        env.registerService(this);
        this.env = env;
    }

    public static Valkyrie getInstrument(Engine engine){
        Instrument instrument = engine.getInstruments().get("Valkyrie");
        if (instrument == null) {
            throw new IllegalStateException("Instrument is not installed.");
        }
        return instrument.lookup(Valkyrie.class);
    }

    public Env getEnv() {
        return this.env;
    }

//    class ValkyrieInstrumentListener implements ExecutionEventListener {
//
//        ValkyrieInstrumentListener() {
//
//        }
//        public void onEnter(EventContext context, VirtualFrame frame) {
////                System.out.println("Entered onEnter in ExecutionEventListener");
//            CompilerDirectives.transferToInterpreter();
//            // notify the runtime that we will change the current execution flow
//            throw context.createUnwind(null);
//        }
//
//        // @Override
//        public void onReturnValue(EventContext context, VirtualFrame frame, Object result) {
////                System.out.println("Entered onReturnValue in ExecutionEventListener");
//            CompilerDirectives.transferToInterpreter();
//            // notify the runtime that we will change the current execution flow
//            throw context.createUnwind(null);
//
//        }
//
//        //@Override
//        public void onReturnExceptional(EventContext context, VirtualFrame frame, Throwable exception) {
////                System.out.println("Entered onReturnExceptional for ExecutionEventListener");
//
//        }
//
////            //@Override
////            public Object onUnwind(EventContext context, VirtualFrame frame, Object info) {
////                return 42;
////            }
//    }

    @Override
    protected void onDispose(TruffleInstrument.Env env) {

    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new ValkyrieOptionOptionDescriptors();
    }
}
