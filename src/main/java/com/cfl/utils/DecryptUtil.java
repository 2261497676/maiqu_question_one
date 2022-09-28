package com.cfl.utils;

import org.springframework.boot.web.servlet.server.Encoding;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.AudioFormat;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DecryptUtil {
    public static String decrypt(String encrypted) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // 1）读取推送的BASE64数据为byte[] encryptedData;
        byte[] encryptedData = DatatypeConverter.parseBase64Binary(encrypted);
        if (encryptedData == null || encryptedData.length < 17) {
            return null;
        }

        // 2）取AES加解密密钥作为AES解密的KEY
        String keyDemo = "b15e3a042de9406e8b097e9837bf01c5";
        byte[] key = keyDemo.getBytes(StandardCharsets.UTF_8);

        // 3) 取byte[] encryptedData的前16位做为IV；
        byte[] iv = new byte[16];
        System.arraycopy(key, 0, iv, 0, 16);

        // 4）取第16位后的字节数组做为待解密内容；
        System.arraycopy(encryptedData, 16, encryptedData, 0, encryptedData.length - 16);

        // 7）使用配置好的实例化AES对象执行解密；
        byte[] r = decryptECB(encryptedData, key);
        // 8）使用UTF-8的方式，读取二进制数组得到原始数据
        return new String(r, StandardCharsets.UTF_8);
    }

    public static byte[] decryptECB(byte[] data, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        // 5）解密模式使用CBC（密码块链模式）；
        // 6）填充模式使用PKCS #7（填充字符串由一个字节序列组成，每个字节填充该字节序列的长度）；
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] result = cipher.doFinal(data);
        return result;
    }
}


