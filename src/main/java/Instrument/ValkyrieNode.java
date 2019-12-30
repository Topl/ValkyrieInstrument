package Instrument;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.js.runtime.JSArguments;

import java.util.Iterator;

/**
 * AST Nodes defined by the instrument filter are wrapped by this node to perform operations on the input data taken
 * from the frame given by the selected {@link com.oracle.truffle.api.source.SourceSection}
 */
final class ValkyrieNode extends ExecutionEventNode {

    private final ValkyrieInstrument instrument;
    private final ProgramController controller;
    private final EventContext context;

    ValkyrieNode(ValkyrieInstrument instrument, ProgramController controller, EventContext context) {
        this.instrument = instrument;
        this.controller = controller;
        this.context = context;
    }


    @Override
    protected void onEnter(VirtualFrame frame) {
        try {
/*
            Object arg1 = frame.getArguments()[0];
            Object arg2 = frame.getArguments()[1];
            System.out.println(((DynamicObject) arg2).getShape());
            System.out.println(((DynamicObject) arg2).get("functionData"));

            Object functionObj = JSArguments.getFunctionObject(frame.getArguments());
            String methodName = JSFunction.getCallTarget((DynamicObject) functionObj).toString();
*/

            Node node = context.getInstrumentedNode();
            Iterator<Node> nodeIterator = node.getChildren().iterator();
            String methodName = nodeIterator.next().getSourceSection().getCharacters().toString();

            if (methodName.contains(".")) {
                String[] separatedNames = methodName.split("\\.");
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
