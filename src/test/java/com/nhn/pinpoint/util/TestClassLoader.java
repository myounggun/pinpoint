package com.nhn.pinpoint.util;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.logging.LoggingUtils;
import com.nhn.pinpoint.DefaultAgent;
import com.nhn.pinpoint.context.DefaultTrace;
import com.nhn.pinpoint.context.DefaultTraceContext;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.modifier.Modifier;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhn.pinpoint.profiler.util.StringUtils;
import com.nhn.pinpoint.util.bindvalue.BindValueConverter;
import javassist.CannotCompileException;
import javassist.Loader;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClassLoader extends Loader {
    private final Logger logger = LoggerFactory.getLogger(TestClassLoader.class.getName());

    private ByteCodeInstrumentor instrumentor;
    private InstrumentTranslator instrumentTranslator;
    private Agent agent;


    public TestClassLoader(Agent agent) {
        this.instrumentor = new JavaAssistByteCodeInstrumentor(null, agent);
        this.instrumentTranslator = new InstrumentTranslator(this);
        this.agent = agent;
    }


    public void initialize() {
        addDefaultDelegateLoadingOf();
        addTranslator();
    }

    public Agent getAgent() {
        return agent;
    }

    public ByteCodeInstrumentor getInstrumentor() {
        return instrumentor;
    }

    public Modifier addModifier(Modifier modifier) {
        return this.instrumentTranslator.addModifier(modifier);
    }

    private void addDefaultDelegateLoadingOf() {
        // 패키지명 필터로 바꾸던지 개선해야 될것으로 보임.
        this.delegateLoadingOf(Interceptor.class.getName());
        this.delegateLoadingOf(StaticAroundInterceptor.class.getName());
        this.delegateLoadingOf(SimpleAroundInterceptor.class.getName());
        this.delegateLoadingOf(InterceptorRegistry.class.getName());
        this.delegateLoadingOf(Trace.class.getName());
        this.delegateLoadingOf(DefaultTrace.class.getName());
        this.delegateLoadingOf(MetaObject.class.getName());
        this.delegateLoadingOf(StringUtils.class.getName());
        this.delegateLoadingOf(MethodDescriptor.class.getName());
        this.delegateLoadingOf(ByteCodeMethodDescriptorSupport.class.getName());
        this.delegateLoadingOf(LoggingUtils.class.getName());
        this.delegateLoadingOf(DefaultAgent.class.getName());
        this.delegateLoadingOf(TraceContext.class.getName());
        this.delegateLoadingOf(DefaultTraceContext.class.getName());



        this.delegateLoadingOf(BindValueConverter.class.getPackage() + ".");
    }

    @Override
    protected Class loadClassByDelegation(String name) throws ClassNotFoundException {
        return super.loadClassByDelegation(name);
    }

    private void addTranslator() {
        try {
            addTranslator(((JavaAssistByteCodeInstrumentor)instrumentor).getClassPool(), instrumentTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void runTest(String className, String methodName) throws Throwable {
        Class c = loadClass(className);
        Object o = c.newInstance();
        try {
            c.getDeclaredMethod(methodName).invoke(o);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
