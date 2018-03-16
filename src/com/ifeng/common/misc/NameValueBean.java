package com.ifeng.common.misc;

import java.io.Serializable;

/**
 * Provide a simple Bean presents Name-Value pair.<br>
 * <p/>
 *
 * @author jinmy
 */
public class NameValueBean implements Serializable {

	private static final long serialVersionUID = 2347344944584478352L;
	private String name;
    private Object value;

    public NameValueBean() {
    }

    public NameValueBean(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    // Getters:
    public String getName() {
        return name;
    }
    public Object getValue() {
        return value;
    }

    // Setters:
    public void setName(String name) {
        this.name = name;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NameValueBean)) {
            return false;
        }
        NameValueBean that = (NameValueBean)obj;
        return (name == null ? that.name == null : name.equals(that.name))
                && (value == null ? that.value == null : value
                        .equals(that.value));
    }

    public int hashCode() {
        int result = 17;
        result = result * 37 + (name == null ? 0 : name.hashCode());
        result = result * 37 + (value == null ? 0 : value.hashCode());
        return result;
    }

    public String toString() {
        return "name=\"" + name + "\",value=\"" + value + "\"";
    }
}
