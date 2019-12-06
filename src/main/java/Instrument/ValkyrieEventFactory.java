package Instrument;

import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;

/**
 * Each time an AST node containing a Valkyrie function is reached, a new ValkyrieNode is created by the instrument
 * to handle the function call.
 */
final class ValkyrieEventFactory implements ExecutionEventNodeFactory {

    private ValkyrieInstrument valkyrieInstrument;

    ValkyrieEventFactory(ValkyrieInstrument valkyrieInstrument) {
        this.valkyrieInstrument = valkyrieInstrument;
    }

    /**
     *
     * @param ec Context of the event, to pass the specific code source the node is instrumenting
     * @return An {@link ExecutionEventNode}
     */
    public ExecutionEventNode create(final EventContext ec) {
        return new ValkyrieNode(valkyrieInstrument, ec.getInstrumentedSourceSection());
    }
}
