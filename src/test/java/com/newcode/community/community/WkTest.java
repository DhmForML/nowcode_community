package com.newcode.community.community;

import java.io.IOException;

public class WkTest {

    public static void main(String[] args) {
        String cmd = "d:/dev_cool/wkhtmltopdf/bin/wkhtmltoimage --quality 75 " +
                "https://www.nowcoder.com d:/dev_cool/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
