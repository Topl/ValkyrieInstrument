package Instrument;

import com.oracle.truffle.api.instrumentation.TruffleInstrument;

public interface ServiceFactory<T> {
    /**
     * Single method used to create instances of the template argument.
     *
     * @param env environment info available to template argument constructor.
     * @return A new instance of the template argument.
     * @since 0.30
     */
    T create(TruffleInstrument.Env env);
}
