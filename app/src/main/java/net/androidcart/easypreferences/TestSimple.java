package net.androidcart.easypreferences;

/**
 * Created by Amin Amini on 6/14/18.
 */

public class TestSimple {
    int sInt;
    String sString;
    float sFloat;

    public TestSimple() {
    }

    public TestSimple(int sInt, String sString, float sFloat) {
        this.sInt = sInt;
        this.sString = sString;
        this.sFloat = sFloat;
    }

    public int getsInt() {
        return sInt;
    }

    public void setsInt(int sInt) {
        this.sInt = sInt;
    }

    public String getsString() {
        return sString;
    }

    public void setsString(String sString) {
        this.sString = sString;
    }

    public float getsFloat() {
        return sFloat;
    }

    public void setsFloat(float sFloat) {
        this.sFloat = sFloat;
    }

    @Override
    public String toString() {
        return "sInt: " + sInt + " , sString : " + sString + " , sFloat : " + sFloat;
    }
}
