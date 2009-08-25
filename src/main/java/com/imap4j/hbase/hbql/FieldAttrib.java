package com.imap4j.hbase.hbql;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 19, 2009
 * Time: 6:07:31 PM
 */
public class FieldAttrib {

    private final Field field;
    private final FieldType fieldType;
    private final HBColumn column;

    private Method getterMethod = null;
    private Method setterMethod = null;

    public FieldAttrib(final Class enclosingClass, final Field field, final HBColumn column) throws HBPersistException {

        this.field = field;
        this.fieldType = FieldType.getFieldType(this.field);
        this.column = column;

        try {
            if (this.hasGetter()) {
                this.getterMethod = enclosingClass.getDeclaredMethod(this.column.getter());

                // Check return type of getter
                final Class<?> returnType = this.getGetterMethod().getReturnType();

                if (!(returnType.isArray() && returnType.getComponentType() == Byte.TYPE))
                    throw new HBPersistException(enclosingClass.getName() + "." + this.column.getter() + "()"
                                                 + " does not have a return type of byte[]");
            }
        }
        catch (NoSuchMethodException e) {
            throw new HBPersistException("Missing method byte[] " + enclosingClass.getName() + "."
                                         + this.column.getter() + "()");
        }

        try {
            if (this.hasSetter()) {

                this.setterMethod = enclosingClass.getDeclaredMethod(this.column.setter(), Class.forName("[B"));

                // Check if it takes single byte[] arg
                final Class<?>[] args = this.getSetterMethod().getParameterTypes();
                if (args.length != 1 || !(args[0].isArray() && args[0].getComponentType() == Byte.TYPE))
                    throw new HBPersistException(enclosingClass.getName() + "." + this.column.setter() + "()"
                                                 + " does not have single byte[] arg");
            }
        }
        catch (NoSuchMethodException e) {
            throw new HBPersistException("Missing method " + enclosingClass.getName() + "." + this.column.setter()
                                         + "(byte[] arg)");
        }
        catch (ClassNotFoundException e) {
            // This will not be hit
            throw new HBPersistException("Missing method " + enclosingClass.getName() + "." + this.column.setter()
                                         + "(byte[] arg)");
        }
    }

    @Override
    public String toString() {
        return this.getField().getDeclaringClass() + "." + this.getFieldName();
    }

    public String getFieldName() {
        return this.getField().getName();
    }

    public boolean isKey() {
        return this.column.key();
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    private Method getGetterMethod() {
        return this.getterMethod;
    }

    private Method getSetterMethod() {
        return this.setterMethod;
    }

    public boolean hasGetter() {
        return this.column.getter().length() > 0;
    }

    public boolean hasSetter() {
        return this.column.setter().length() > 0;
    }

    public String getFamilyName() {
        return this.column.family();
    }

    public String getColumnName() {
        return this.column.column().length() > 0 ? column.column() : this.getFieldName();
    }

    public String getQualifiedName() {
        return this.getFamilyName() + ":" + this.getColumnName();
    }

    public Field getField() {
        return field;
    }

    public boolean isArray() {
        return this.getField().getType().isArray();
    }

    public boolean isMapKeysAsColumns() {
        return this.column.mapKeysAsColumns();
    }

    public byte[] invokeGetterMethod(final Object parent) throws HBPersistException {
        try {
            return (byte[])this.getGetterMethod().invoke(parent);
        }
        catch (IllegalAccessException e) {
            throw new HBPersistException("Error getting value of " + this.getFieldName());
        }
        catch (InvocationTargetException e) {
            throw new HBPersistException("Error getting value of " + this.getFieldName());
        }
    }

    public Object invokeSetterMethod(final Object parent, final byte[] b) throws HBPersistException {
        try {
            return this.getSetterMethod().invoke(parent, b);
        }
        catch (IllegalAccessException e) {
            throw new HBPersistException("Error setting value of " + this.getFieldName());
        }
        catch (InvocationTargetException e) {
            throw new HBPersistException("Error setting value of " + this.getFieldName());
        }
    }

    public Object getValue(final HBPersistable declaringObj) throws HBPersistException {
        try {
            return this.getField().get(declaringObj);
        }
        catch (IllegalAccessException e) {
            throw new HBPersistException("Error getting value of " + this.getFieldName());
        }

    }

    public byte[] getValueAsBytes(final HBPersistable declaringObj) throws HBPersistException, IOException {

        if (this.hasGetter()) {
            return this.invokeGetterMethod(declaringObj);
        }
        else {
            final Object obj = this.getValue(declaringObj);

            if (this.isArray())
                return HBUtil.getArrayasBytes(this.getFieldType(), obj);
            else
                return HBUtil.getScalarAsBytes(this.getFieldType(), obj);
        }
    }

    public Object getValueFromBytes(final HBPersistable declaringObj, final byte[] b) throws IOException, HBPersistException {

        if (this.hasSetter()) {
            return this.invokeSetterMethod(declaringObj, b);
        }
        else {
            if (this.isArray())
                return HBUtil.getArrayFromBytes(this.getFieldType(), this.getField().getType().getComponentType(), b);
            else
                return HBUtil.getScalarFromBytes(this.getFieldType(), b);
        }
    }

}
