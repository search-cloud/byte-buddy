package com.blogspot.mydailyjava.bytebuddy.method.matcher;

import com.blogspot.mydailyjava.bytebuddy.method.MethodDescription;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class MethodMatchers {

    private static enum MatchMode {

        EQUALS_FULLY,
        EQUALS_FULLY_IGNORE_CASE,
        STARTS_WITH,
        STARTS_WITH_IGNORE_CASE,
        ENDS_WITH,
        ENDS_WITH_IGNORE_CASE,
        CONTAINS,
        CONTAINS_IGNORE_CASE,
        MATCHES;

        private boolean matches(String left, String right) {
            switch (this) {
                case EQUALS_FULLY:
                    return right.equals(left);
                case EQUALS_FULLY_IGNORE_CASE:
                    return right.equalsIgnoreCase(left);
                case STARTS_WITH:
                    return right.startsWith(left);
                case STARTS_WITH_IGNORE_CASE:
                    return right.toLowerCase().startsWith(left.toLowerCase());
                case ENDS_WITH:
                    return right.endsWith(left);
                case ENDS_WITH_IGNORE_CASE:
                    return right.toLowerCase().endsWith(left.toLowerCase());
                case CONTAINS:
                    return right.contains(left);
                case CONTAINS_IGNORE_CASE:
                    return right.toLowerCase().contains(left.toLowerCase());
                case MATCHES:
                    return right.matches(left);
                default:
                    throw new AssertionError("Unknown match mode: " + this);
            }
        }
    }

    private static class ClassNameMethodMatcher extends JunctionMethodMatcher {

        private final Class<?> type;

        public ClassNameMethodMatcher(Class<?> type) {
            this.type = type;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            try {
                return methodDescription.getDeclaringClass() == type || type.getDeclaredMethod(methodDescription.getName(), methodDescription.getParameterTypes()) != null;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    }

    public static JunctionMethodMatcher declaredIn(Class<?> type) {
        return new ClassNameMethodMatcher(type);
    }

    private static class MethodNameMethodMatcher extends JunctionMethodMatcher {

        private final String methodName;
        private final MatchMode matchMode;

        public MethodNameMethodMatcher(String methodName, MatchMode matchMode) {
            this.methodName = methodName;
            this.matchMode = matchMode;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return matchMode.matches(methodName, methodDescription.getName());
        }
    }

    public static JunctionMethodMatcher named(String name) {
        return new MethodNameMethodMatcher(name, MatchMode.EQUALS_FULLY);
    }

    public static JunctionMethodMatcher namedIgnoreCase(String name) {
        return new MethodNameMethodMatcher(name, MatchMode.EQUALS_FULLY_IGNORE_CASE);
    }

    public static JunctionMethodMatcher nameStartsWith(String prefix) {
        return new MethodNameMethodMatcher(prefix, MatchMode.STARTS_WITH);
    }

    public static JunctionMethodMatcher nameStartsWithIgnoreCase(String name) {
        return new MethodNameMethodMatcher(name, MatchMode.STARTS_WITH_IGNORE_CASE);
    }

    public static JunctionMethodMatcher nameEndsWith(String suffix) {
        return new MethodNameMethodMatcher(suffix, MatchMode.ENDS_WITH);
    }

    public static JunctionMethodMatcher nameEndsWithIgnoreCase(String suffix) {
        return new MethodNameMethodMatcher(suffix, MatchMode.ENDS_WITH_IGNORE_CASE);
    }

    public static JunctionMethodMatcher nameContains(String contains) {
        return new MethodNameMethodMatcher(contains, MatchMode.CONTAINS);
    }

    public static JunctionMethodMatcher nameContainsIgnoreCase(String contains) {
        return new MethodNameMethodMatcher(contains, MatchMode.CONTAINS_IGNORE_CASE);
    }

    public static JunctionMethodMatcher matches(String regex) {
        return new MethodNameMethodMatcher(regex, MatchMode.MATCHES);
    }

    private static class ModifierMethodMatcher extends JunctionMethodMatcher {

        private final int modifierMask;

        public ModifierMethodMatcher(int modifierMask) {
            this.modifierMask = modifierMask;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return (methodDescription.getModifiers() & modifierMask) != 0;
        }
    }

    public static JunctionMethodMatcher isPublic() {
        return new ModifierMethodMatcher(Modifier.PUBLIC);
    }

    public static JunctionMethodMatcher isProtected() {
        return new ModifierMethodMatcher(Modifier.PROTECTED);
    }

    public static JunctionMethodMatcher isPackagePrivate() {
        return not(isPublic().or(isProtected()).or(isPrivate()));
    }

    public static JunctionMethodMatcher isPrivate() {
        return new ModifierMethodMatcher(Modifier.PRIVATE);
    }

    public static JunctionMethodMatcher isFinal() {
        return new ModifierMethodMatcher(Modifier.FINAL);
    }

    public static JunctionMethodMatcher isStatic() {
        return new ModifierMethodMatcher(Modifier.STATIC);
    }

    public static JunctionMethodMatcher isSynchronized() {
        return new ModifierMethodMatcher(Modifier.SYNCHRONIZED);
    }

    public static JunctionMethodMatcher isNative() {
        return new ModifierMethodMatcher(Modifier.NATIVE);
    }

    public static JunctionMethodMatcher isStrict() {
        return new ModifierMethodMatcher(Modifier.STRICT);
    }

    private static class VarArgsMethodMatcher extends JunctionMethodMatcher {

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.isVarArgs();
        }
    }

    public static JunctionMethodMatcher isVarArgs() {
        return new VarArgsMethodMatcher();
    }

    private static class SyntheticMethodMatcher extends JunctionMethodMatcher {

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.isSynthetic();
        }
    }

    public static JunctionMethodMatcher isSynthetic() {
        return new SyntheticMethodMatcher();
    }

    private static class BridgeMethodMatcher extends JunctionMethodMatcher {

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.isBridge();
        }
    }

    public static JunctionMethodMatcher isBridge() {
        return new BridgeMethodMatcher();
    }

    private static class ReturnTypeMatcher extends JunctionMethodMatcher {

        private final Class<?> returnType;

        public ReturnTypeMatcher(Class<?> returnType) {
            this.returnType = returnType;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.getReturnType() == returnType;
        }
    }

    public static JunctionMethodMatcher returns(Class<?> type) {
        return new ReturnTypeMatcher(type);
    }

    private static class ParameterTypeMatcher extends JunctionMethodMatcher {

        private final Class<?>[] parameterType;

        public ParameterTypeMatcher(Class<?>[] parameterType) {
            this.parameterType = parameterType;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return Arrays.equals(parameterType, methodDescription.getParameterTypes());
        }
    }

    public static JunctionMethodMatcher takesArguments(Class<?>... types) {
        return new ParameterTypeMatcher(types);
    }

    private static class ExceptionMethodMatcher extends JunctionMethodMatcher {

        private final Class<?> exceptionType;

        public ExceptionMethodMatcher(Class<?> exceptionType) {
            this.exceptionType = exceptionType;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            for (Class<?> exceptionType : methodDescription.getExceptionTypes()) {
                if (exceptionType.isAssignableFrom(this.exceptionType)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static JunctionMethodMatcher canThrow(Class<? extends Exception> exceptionType) {
        return new ExceptionMethodMatcher(exceptionType);
    }

    private static class MethodEqualityMethodMatcher extends JunctionMethodMatcher {

        private final Method method;

        public MethodEqualityMethodMatcher(Method method) {
            this.method = method;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.represents(method);
        }
    }

    public static JunctionMethodMatcher is(Method method) {
        return new MethodEqualityMethodMatcher(method);
    }

    private static class ConstructorEqualityMethodMatcher extends JunctionMethodMatcher {

        private final Constructor<?> constructor;

        public ConstructorEqualityMethodMatcher(Constructor<?> constructor) {
            this.constructor = constructor;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.represents(constructor);
        }
    }

    public static JunctionMethodMatcher is(Constructor<?> constructor) {
        return new ConstructorEqualityMethodMatcher(constructor);
    }

    private static class PackageNameMatcher extends JunctionMethodMatcher {

        private final String packageName;

        private PackageNameMatcher(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.getDeclaringClass().getPackage().getName().equals(packageName);
        }
    }

    public static JunctionMethodMatcher isDefinedInPackage(String packageName) {
        return new PackageNameMatcher(packageName);
    }

    private static class DefaultFinalizeMethodMatcher extends JunctionMethodMatcher {

        private static final String FINALIZE_METHOD_NAME = "finalize";

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return methodDescription.getDeclaringClass() == Object.class
                    && methodDescription.getName().equals(FINALIZE_METHOD_NAME)
                    && methodDescription.getParameterTypes().length == 0;
        }
    }

    public static JunctionMethodMatcher isDefaultFinalize() {
        return new DefaultFinalizeMethodMatcher();
    }

    private static class NegatingMethodMatcher extends JunctionMethodMatcher {

        private final MethodMatcher methodMatcher;

        public NegatingMethodMatcher(MethodMatcher methodMatcher) {
            this.methodMatcher = methodMatcher;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return !methodMatcher.matches(methodDescription);
        }
    }

    public static JunctionMethodMatcher not(MethodMatcher methodMatcher) {
        return new NegatingMethodMatcher(methodMatcher);
    }

    private static class BooleanMethodMatcher extends JunctionMethodMatcher {

        private final boolean matches;

        private BooleanMethodMatcher(boolean matches) {
            this.matches = matches;
        }

        @Override
        public boolean matches(MethodDescription methodDescription) {
            return matches;
        }
    }

    public static JunctionMethodMatcher any() {
        return new BooleanMethodMatcher(true);
    }

    public static JunctionMethodMatcher none() {
        return new BooleanMethodMatcher(false);
    }

    private MethodMatchers() {
        throw new AssertionError();
    }
}
