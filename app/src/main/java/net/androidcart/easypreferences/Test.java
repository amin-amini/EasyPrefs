package net.androidcart.easypreferences;

import java.util.ArrayList;

/**
 * Created by Amin Amini on 6/14/18.
 */

public class Test {
    int mInt;
    String mString;
    ArrayList<TestSimple> mSimples;

    public Test() {
        mInt = 123;
        mString = "stringTest";
        mSimples = new ArrayList<>();
        mSimples.add(new TestSimple(12,"simple12" , 12.5f));
        mSimples.add(new TestSimple(13,"simple13" , 13.5f));
    }

    public Test(int mInt, String mString, ArrayList<TestSimple> mSimples) {
        this.mInt = mInt;
        this.mString = mString;
        this.mSimples = mSimples;
    }

    public int getmInt() {
        return mInt;
    }

    public void setmInt(int mInt) {
        this.mInt = mInt;
    }

    public String getmString() {
        return mString;
    }

    public void setmString(String mString) {
        this.mString = mString;
    }

    public ArrayList<TestSimple> getmSimples() {
        return mSimples;
    }

    public void setmSimples(ArrayList<TestSimple> mSimples) {
        this.mSimples = mSimples;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("mInt : ").append(mInt).append("\n")
                .append("mString : ").append(mString).append("\n");
        for (TestSimple ts : getmSimples()){
            builder.append("simple : ").append(ts).append("\n");
        }
        return builder.toString();
    }
}
