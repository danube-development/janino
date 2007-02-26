
/*
 * Janino - An embedded Java[TM] compiler
 *
 * Copyright (c) 2006, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.codehaus.janino;

/**
 * Helper class that defines useful methods for handling "field descriptors"
 * (JVMS 4.3.2) and "method descriptors" (JVMS 4.3.3).<p>
 * Typical descriptors are:
 * <ul>
 *   <li><code>I</code> Integer
 *   <li><code>[I</code> Array of integer
 *   <li><code>Lpkg1/pkg2/Cls;</code> Class
 *   <li><code>Lpkg1/pkg2/Outer$Inner;</code> Member class
 * </ul>
 */
public class Descriptor {
    public static boolean isReference(String d) {
        return d.length() > 1;
    }
    public static boolean isClassOrInterfaceReference(String d) {
        return d.charAt(0) == 'L';
    }
    public static boolean isArrayReference(String d) {
        return d.charAt(0) == '[';
    }
    public static String getComponentDescriptor(String d) {
        if (d.charAt(0) != '[') throw new RuntimeException("Cannot determine component descriptor from non-array descriptor \"" + d + "\"");
        return d.substring(1);
    }
    public static short size(String d) {
        if (d.equals(Descriptor.VOID_)) return 0;
        if (Descriptor.hasSize1(d)) return 1;
        if (Descriptor.hasSize2(d)) return 2;
        throw new RuntimeException("No size defined for type \"" + Descriptor.toString(d) + "\"");
    }
    public static boolean hasSize1(String d) {
        if (d.length() == 1) return "BCFISZ".indexOf(d) != -1;
        return Descriptor.isReference(d);
    }
    public static boolean hasSize2(String d) {
        return d.equals(Descriptor.LONG_) || d.equals(Descriptor.DOUBLE_);
    }

    // Pretty-print.
    public static String toString(String d) {
        int idx = 0;
        StringBuffer sb = new StringBuffer();
        if (d.charAt(0) == '(') {
            ++idx;
            sb.append("(");
            while (idx < d.length() && d.charAt(idx) != ')') {
                if (idx != 1) sb.append(", ");
                idx = Descriptor.toString(d, idx, sb);
            }
            if (idx >= d.length()) throw new RuntimeException("Invalid descriptor \"" + d + "\"");
            sb.append(") => ");
            ++idx;
        }
        Descriptor.toString(d, idx, sb);
        return sb.toString();
    }
    private static int toString(String d, int idx, StringBuffer sb) {
        int dimensions = 0;
        while (idx < d.length() && d.charAt(idx) == '[') {
            ++dimensions;
            ++idx;
        }
        if (idx >= d.length()) throw new RuntimeException("Invalid descriptor \"" + d + "\"");
        switch (d.charAt(idx)) {
        case 'L':
            {
                int idx2 = d.indexOf(';', idx);
                if (idx2 == -1) throw new RuntimeException("Invalid descriptor \"" + d + "\"");
                sb.append(d.substring(idx + 1, idx2).replace('/', '.'));
                idx = idx2;
            }
            break;
        case 'V': sb.append("void");    break;
        case 'B': sb.append("byte");    break;
        case 'C': sb.append("char");    break;
        case 'D': sb.append("double");  break;
        case 'F': sb.append("float");   break;
        case 'I': sb.append("int");     break;
        case 'J': sb.append("long");    break;
        case 'S': sb.append("short");   break;
        case 'Z': sb.append("boolean"); break;
        default:
            throw new RuntimeException("Invalid descriptor \"" + d + "\"");
        }
        for (; dimensions > 0; --dimensions) sb.append("[]");
        return idx + 1;
    }

    /**
     * Convert a class name as defined by "Class.getName()" into a
     * descriptor.
     */
    public static String fromClassName(String className) {
        if (className.equals("void"   )) return Descriptor.VOID_;
        if (className.equals("byte"   )) return Descriptor.BYTE_;
        if (className.equals("char"   )) return Descriptor.CHAR_;
        if (className.equals("double" )) return Descriptor.DOUBLE_;
        if (className.equals("float"  )) return Descriptor.FLOAT_;
        if (className.equals("int"    )) return Descriptor.INT_;
        if (className.equals("long"   )) return Descriptor.LONG_;
        if (className.equals("short"  )) return Descriptor.SHORT_;
        if (className.equals("boolean")) return Descriptor.BOOLEAN_;
        if (className.startsWith("[")) return className.replace('.', '/');
        return 'L' + className.replace('.', '/') + ';';
    }

    /**
     * Convert a class name in the "internal form" as described in JVMS 4.2 into a descriptor.
     * <p>
     * Also implement the encoding of array types as described in JVMS 4.4.1.
     */
    public static String fromInternalForm(String internalForm) {
        if (internalForm.charAt(0) == '[') return internalForm;
        return 'L' + internalForm + ';';
    }

    /**
     * Convert a field descriptor into a class name as defined by {@link
     * Class#getName()}.
     */
    public static String toClassName(String d) {
        if (d.length() == 1) {
            if (d.equals(Descriptor.VOID_   )) return "void";
            if (d.equals(Descriptor.BYTE_   )) return "byte";
            if (d.equals(Descriptor.CHAR_   )) return "char";
            if (d.equals(Descriptor.DOUBLE_ )) return "double";
            if (d.equals(Descriptor.FLOAT_  )) return "float";
            if (d.equals(Descriptor.INT_    )) return "int";
            if (d.equals(Descriptor.LONG_   )) return "long";
            if (d.equals(Descriptor.SHORT_  )) return "short";
            if (d.equals(Descriptor.BOOLEAN_)) return "boolean";
        } else {
            char firstChar = d.charAt(0);
            if (firstChar == 'L' && d.endsWith(";")) {

                // Class or interface -- convert "Ljava/lang/String;" to "java.lang.String".
                return d.substring(1, d.length() - 1).replace('/', '.');
            } 
            if (firstChar == '[') {

                // Array type -- convert "[Ljava/lang/String;" to "[Ljava.lang.String;".
                return d.replace('/', '.');
            } 
        }
        throw new RuntimeException("(Invalid field descriptor \"" + d + "\")");
    }

    /**
     * Convert a descriptor into the "internal form" as defined by JVMS 4.2.
     */
    public static String toInternalForm(String d) {
        if (d.charAt(0) != 'L') throw new RuntimeException("Attempt to convert non-class descriptor \"" + d + "\" into internal form");
        return d.substring(1, d.length() - 1);
    }

    public static boolean isPrimitive(String d) {
        return d.length() == 1 && "VBCDFIJSZ".indexOf(d.charAt(0)) != -1;
    }

    public static boolean isPrimitiveNumeric(String d) {
        return d.length() == 1 && "BDFIJSC".indexOf(d.charAt(0)) != -1;
    }

    /**
     * Returns the package name of a class or interface reference descriptor,
     * or <code>null</code> if the class or interface is declared in the
     * default package.
     */
    public static String getPackageName(String d) {
        if (d.charAt(0) != 'L') throw new RuntimeException("Attempt to get package name of non-class descriptor \"" + d + "\"");
        int idx = d.lastIndexOf('/');
        return idx == -1 ? null : d.substring(1, idx).replace('/', '.');
    }

    /**
     * Check whether two reference types are declared in the same package.
     */
    public static boolean areInSamePackage(String d1, String d2) {
        String packageName1 = Descriptor.getPackageName(d1);
        String packageName2 = Descriptor.getPackageName(d2);
        return packageName1 == null ? packageName2 == null : packageName1.equals(packageName2);
    }

    public final static String VOID_    = "V";
    public final static String BYTE_    = "B";
    public final static String CHAR_    = "C";
    public final static String DOUBLE_  = "D";
    public final static String FLOAT_   = "F";
    public final static String INT_     = "I";
    public final static String LONG_    = "J";
    public final static String SHORT_   = "S";
    public final static String BOOLEAN_ = "Z";
    public final static String OBJECT            = "Ljava/lang/Object;";
    public final static String STRING            = "Ljava/lang/String;";
    public final static String STRING_BUFFER     = "Ljava/lang/StringBuffer;";
    public final static String STRING_BUILDER    = "Ljava/lang/StringBuilder;"; // Since 1.5!
    public final static String CLASS             = "Ljava/lang/Class;";
    public final static String THROWABLE         = "Ljava/lang/Throwable;";
    public final static String RUNTIME_EXCEPTION = "Ljava/lang/RuntimeException;";
    public final static String ERROR             = "Ljava/lang/Error;";
    public final static String CLONEABLE         = "Ljava/lang/Cloneable;";
    public final static String SERIALIZABLE      = "Ljava/io/Serializable;";
    public final static String BOOLEAN   = "Ljava/lang/Boolean;";
    public final static String BYTE      = "Ljava/lang/Byte;";
    public final static String CHARACTER = "Ljava/lang/Character;";
    public final static String SHORT     = "Ljava/lang/Short;";
    public final static String INTEGER   = "Ljava/lang/Integer;";
    public final static String LONG      = "Ljava/lang/Long;";
    public final static String FLOAT     = "Ljava/lang/Float;";
    public final static String DOUBLE    = "Ljava/lang/Double;";
}
