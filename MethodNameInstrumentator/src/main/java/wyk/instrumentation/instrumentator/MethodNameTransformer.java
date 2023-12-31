package wyk.instrumentation.instrumentator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MethodNameTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] result = classfileBuffer;
        if (className.equals("wyk/instrumentation/target/Main")) {
            result = this.insertMethodName(classfileBuffer);
        }
        return result;
    }

    protected byte[] insertMethodName(final byte[] classfileBuffer) {
        try {
            ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), null);
            JavaClass javaClass = parser.parse();
            ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
            InstructionFactory factory = new InstructionFactory(constantPoolGen);

            ClassGen classGen = new ClassGen(javaClass);
            for (Method method : javaClass.getMethods()) {
                MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), constantPoolGen);
                InstructionList originalList = methodGen.getInstructionList();
                originalList.insert(factory.createPrintln("Method name: " + method.getName()));

                methodGen.setInstructionList(originalList);
                methodGen.setMaxStack();

                classGen.replaceMethod(method, methodGen.getMethod());
                originalList.dispose();
            }

            classGen.setConstantPool(constantPoolGen);
            return classGen.getJavaClass().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return classfileBuffer;
        }
    }
}
