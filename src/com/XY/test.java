package com.XY;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

public class test {
    public static String castToSHA3(String str){
        //transferFrom(address _from, address _to, uint256 _value) returns (bool success)
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        String str_ret= Hex.toHexString(digest256.digest(str.getBytes())).substring(0, 8);
        return str_ret;
    }
    public static void main(String[] args) {
        //function test(uint256 a,uint256 b) public payable returns{}
        System.out.println(castToSHA3("test(address,address)"));
    }
}
