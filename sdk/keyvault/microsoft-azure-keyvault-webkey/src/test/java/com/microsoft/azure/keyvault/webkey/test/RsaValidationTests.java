// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.webkey.test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;

public class RsaValidationTests {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    @Test
    public void rsaPublicKeyValidation() throws Exception {
        for (String keyStr : keys.values()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonWebKey key = mapper.readValue(keyStr, JsonWebKey.class);
            Assert.assertTrue(key.hasPrivateKey());
            Assert.assertTrue(key.isValid());

            KeyPair keyPair = key.toRSA();
            validateRsaKey(keyPair, key);
            Assert.assertNull(keyPair.getPrivate());

            // Compare equal JSON web keys
            JsonWebKey sameKey = mapper.readValue(keyStr, JsonWebKey.class);
            Assert.assertEquals(key, key);
            Assert.assertEquals(key, sameKey);
            Assert.assertEquals(key.hashCode(), sameKey.hashCode());
        }
    }

    @Test
    public void rsaPrivateKeyValidation() throws Exception {
        for (String keyStr : keys.values()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonWebKey key = mapper.readValue(keyStr, JsonWebKey.class);

            KeyPair keyPairWithPrivate = key.toRSA(true);
            validateRsaKey(keyPairWithPrivate, key);
            encryptDecrypt(keyPairWithPrivate.getPublic(), keyPairWithPrivate.getPrivate());
        }
    }

    @Test
    public void rsaHashCode() throws Exception {

        String keyStr = (String) keys.values().toArray()[0];
        ObjectMapper mapper = new ObjectMapper();
        JsonWebKey key = mapper.readValue(keyStr, JsonWebKey.class);

        // Compare hash codes for unequal JWK that would not map to the same hash
        Assert.assertNotEquals(key.hashCode(), new JsonWebKey().withKid(key.kid()).withN(key.n()).hashCode());
        Assert.assertNotEquals(key.hashCode(), new JsonWebKey().withKid(key.kid()).withKty(key.kty()).hashCode());
        Assert.assertNotEquals(key.hashCode(), new JsonWebKey().withKid(key.kid()).withT(key.t()).hashCode());

        // Compare hash codes for unequal JWK that would map to the same hash
        Assert.assertEquals(key.hashCode(),
                new JsonWebKey().withN(key.n()).withKty(key.kty()).withKid(key.kid()).hashCode());
    }

    private static void encryptDecrypt(PublicKey publicKey, PrivateKey privateKey) throws Exception {
        byte[] plaintext = new byte[10];
        new Random().nextBytes(plaintext);
        byte[] cipherText = encrypt(publicKey, plaintext);
        if (privateKey != null) {
            Assert.assertArrayEquals(decrypt(privateKey, cipherText), plaintext);
        }
    }

    private static byte[] encrypt(PublicKey key, byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    private static byte[] decrypt(PrivateKey key, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(ciphertext);
    }

    private static void validateRsaKey(KeyPair keyPair, JsonWebKey key) throws Exception {
        JsonWebKey jsonWebKey = JsonWebKey.fromRSA(keyPair);
        boolean includePrivateKey = keyPair.getPrivate() != null;
        KeyPair keyPair2 = jsonWebKey.toRSA(includePrivateKey);

        Assert.assertTrue(includePrivateKey == jsonWebKey.hasPrivateKey());

        PublicKey publicKey = keyPair2.getPublic();
        PrivateKey privateKey = keyPair2.getPrivate();

        if (includePrivateKey) {
            Assert.assertNotNull(privateKey);

            // set the missing properties to compare the keys
            jsonWebKey.withKeyOps(new ArrayList<JsonWebKeyOperation>(key.keyOps()));
            jsonWebKey.withKid(new String(key.kid()));
            Assert.assertEquals(jsonWebKey, key);
            Assert.assertEquals(key.hashCode(), jsonWebKey.hashCode());
        }

        encryptDecrypt(publicKey, privateKey);
    }

    Map<Integer, String> keys = ImmutableMap.<Integer, String>builder()
            .put(512, "{\"kty\":\"RSA\",\"n\":\"uOXIpiH9L0h_byTuP3fcMvKbfS85eTKvxW2skw4oIU2TM3ceFvlDwDK4gKHl4qE4z18bz0qrv9ElstOrT96piQ\",\"e\":\"AQAB\",\"d\":\"And2KMA5uQ1r9MwuvZCODi0D2lcFvz7oBbenyxqmuhTYfdGcuGE9FZg5V6ZcNwBK_eYGZqSwL1Gh2EmzG6AxwQ\",\"dp\":\"CEh8kzQnCRK97NKQeV_wGgWsLYlmgis7Cms85_DIqwE\",\"dq\":\"TAi0G0iE5pvMpiEN2y189hjSRSqE6Unc1lXaE3hcnWE\",\"qi\":\"2HhNqW3QBv1R_iEpu44KVMQs0DdnY5oWp1lH6hgPhXU\",\"p\":\"5BblSoMJmO5Afa-urQFzFpBfACt1175NMUs4tHUYEkE\",\"q\":\"z4Xdf_FU-51wTkW5mFJ6QoDK-GrkMXSdct9hdW26NUk\",\"key_ops\":[\"wrapKey\",\"unwrapKey\",\"encrypt\",\"decrypt\",\"sign\",\"verify\"],\"kid\":\"key_id\"}")
            .put(1024, "{\"kty\":\"RSA\",\"n\":\"zicSNMeAUYwp6V6UQlJ8gW04o6O4ZJBIefsLnV6-to1YkzgDu6vDBWb83DcDgB2x63W-ZVK23F4dcJcULu1VM-jX83Sfg0b_ZrugiiXCnZ4iidLNcY5QOS1dSHjfI1eWH6QdLPSIE3sHk-BILrIXqoyIJH-LFxzMu--4bDlej2M\",\"e\":\"AQAB\",\"d\":\"A4h7F2YT6bhG2TXcJ9OiFQj6LFPLmG2gnSnGssiQHDDWXWLB-mvT-9O4CBr2ETJxFvsw0cVV8CqGXQrTaodGxOuCGNmYoczodvlhUBJyMBxAI2or5eZUF9jRiECvigoxNVWKsqWxypvq_X1pMfQbh9ot7F6KOJAEg6wlLTc-fIE\",\"dp\":\"v2JbDaZfi3OCCLMtNMjOxfNsBOPb1IqerGux4IR17fLIzG6JlcyaR4uasILdjE4VufqnppZ6FIlFCZUiyIP0GQ\",\"dq\":\"m6NTbNOxN2qnont_qttyqg6WvOA6zWK55-ZnX8hShmlv0ySgtw1PfOWso3wpRMHAujTOfUSeI14DgOLHLNkKtQ\",\"qi\":\"HOcBZfyxW1dSnghCvdTuKL3jLSww6k_v0jhYET32gyKe8od7uxP7w0dXZ8al4zQ3xGxrip9y7jJi0pjG-Z4uGw\",\"p\":\"6dlyTUBrwxLyLbr0X3yqmNu3VrHSt2zbW8jueZFWXPELlbuQ6EKrHoR39BM8MSjFN5PfZbsBhcqNBkqhitj6xw\",\"q\":\"4a4DOrnZt4423myMKmhgDINvIdNmLCHG0aE8UWcSPKO6RFhzHX46NJSoOuk9gvccMKEXOpcJC6P8b8ypN-OKhQ\",\"key_ops\":[\"wrapKey\",\"unwrapKey\",\"encrypt\",\"decrypt\",\"sign\",\"verify\"],\"kid\":\"key_id\"}")
            .put(2048, "{\"kty\":\"RSA\",\"n\":\"rZ8pnmXkhfmmgNWVVdtNcYy2q0OAcCGIpeFzsN9URqJsiBEiWQfxlUxFTbM4kVWPqjauKt6byvApBGEeMA7Qs8kxwRVP-BD4orXRe9VPgliM92rH0UxQWHmCHUe7G7uUAFPwbiDVhWuFzELxNa6Kljg6Z9DuUKoddmQvlYWj8uSunofCtDi_zzlZKGYTOYJma5IYScHNww1yjLp8-b-Be2UdHbrPkCv6Nuwi6MVIKjPpEeRQgfefRmxDBJQKY3OfydMXZmEwukYXVkUcdIP8XwG2OxnfdRK0oAo0NDebNNVuT89k_3AyZLTr1KbDmx1nnjwa8uB8k-uLtcOC9igbTw\",\"e\":\"AQAB\",\"d\":\"H-z7hy_vVJ9yeZBMtIvt8qpQUK_J51STPwV085otcgud72tPKJXoW2658664ASl9kGwbnLBwb2G3-SEunuGqiNS_PGUB3niob6sFSUMRKsPDsB9HfPoOcCZvwZiWFGRqs6C7vlR1TuJVqRjKJ_ffbf4K51oo6FZPspx7j4AShLAwLUSQ60Ld5QPuxYMYZIMpdVbMVIVHJ26pR4Y18e_0GYmEGnbF5N0HkwqQmfmTiIK5aoGnD3GGgqHeHmWBwh6_WAq90ITLcX_zBeqQUgBSj-Z5v61SroO9Eang36T9mMoYrcPpYwemtAOb4HhQYDj8dCCfbeOcVmvZ9UJKWCX2oQ\",\"dp\":\"HW87UpwPoj3lPI9B9K1hJFeuGgarpakvtHuk1HpZ5hXWFGAJiXoWRV-jvYyjoM2k7RpSxPyuuFFmYHcIxiGFp2ES4HnP0BIhKVa2DyugUxIEcMK53C43Ub4mboJPZTSC3sapKgAmA2ue624sapWmshTPpx9qnUP2Oj3cSMkgMGE\",\"dq\":\"RhwEwb5FYio0GS2tmul8FAYsNH7JDehwI1yUApnTiakhSenFetml4PYyVkKR4csgLZEi3RY6J3R8Tg-36zrZuF7hxhVJn80L5_KETSpfEI3jcrXMVg4SRaMsWLY9Ahxflt2FJgUnHOmWRLmP6_hmaTcxxSACjbyUd_HhwNavD5E\",\"qi\":\"wYPZ4lKIslA1w3FaAzQifnNLABYXXUZ_KAA3a8T8fuxkdE4OP3xIFX7WHhnmBd6uOFiEcGoeq2jNQqDg91rV5661-5muQKcvp4uUsNId5rQw9EZw-kdDcwMtVFTEBfvVuyp83X974xYAHn1Jd8wWohSwrpi1QuH5cQMR5Fm6I1A\",\"p\":\"74Ot7MgxRu4euB31UWnGtrqYPjJmvbjYESS43jfDfo-s62ggV5a39P_YPg6oosgtGHNw0QDxunUOXNu9iriaYPf_imptRk69bKN8Nrl727Y-AaBYdLf1UZuwz8X07FqHAH5ghYpk79djld8QvkUUJLpx6rzcW8BJLTOi46DtzZE\",\"q\":\"uZJu-qenARIt28oj_Jlsk-p_KLnqdczczZfbRDd7XNp6csGLa8R0EyYqUB4xLWELQZsX4tAu9SaAO62tuuEy5wbOAmOVrq2ntoia1mGQSJdoeVq6OqtN300xVnaBc3us0rm8C6-824fEQ1PWXoulXLKcSqBhFT-hQahsYi-kat8\",\"key_ops\":[\"wrapKey\",\"unwrapKey\",\"encrypt\",\"decrypt\",\"sign\",\"verify\"],\"kid\":\"key_id\"}")
            .put(3072, "{\"kty\":\"RSA\",\"n\":\"03u6K67VN18OzIRZdvCC8F9iOVojF-0kk03JQ7rfwumQMqgxLYOmLkrqLcyJV69XYt32LeEesuwuz_zJbQo9gg4T1pnKJSb-l5xoH1rfnihdc9PyMAH___d_zv3Zg9vdusg668eO1oqS5DtAe517suzwhcMIyCsFNx4aBxCDiPlEwzYISwMQHylt-4d6mbFsqJoGK14WqxTOyv0mLoeeDPs9gmQulGbyjYdZJgqjeRBMuHpXgjs_eMwHuqYmWr-jmbRMzBJpKoAgAJkDxkJzJ7wdf4Bq9HrutVspXqw9ZWh4ImIq65Rm5Mx3JDlUNdlYB0jMyDHpuwAZfr8shACty2d5bvlMnk7aYKngCbX2ZSm6BFInA4mz1eey9Iz8uxnfyEjwaYJCFRDy44P_8aymW4tsLoLYgWnF9NodxcLVbhJjBqsipYiUbvW6PUUB4SVtql4yI3EEcZsFFVAVOnms1sXGXK8vm9V9KU1RSWqF268jMD8s-QHg3a1WmooX6sw5\",\"e\":\"AQAB\",\"d\":\"WKU3m6DcmamcK-jcEUluMTBiHTUlmZ1a4-3Ki7vUmEBLo5gxiOjyatwW_dyKwzjpkbUFQCTpN8ldM-w7SBvvPUkGUsFC5MDMHaO_V0lBi2tTBL6V-T6VXmcRaSOpnaY28liErhkHS_Fo8gbOGCKiW5UKmp7uWu0BciGJemWXJP6LLqJC5qJhixZUFgcrQioHKELrjBkTumFt7tMewokxHDLhjPrONYFTcTSHDzWNYS0OY5NQg_OuvsUTBk8nq4lA2GSQqXyM-B2gbwG6pLSwccwu0x3Fd8qurxg6TSGQAjh69Iyb9ZwiHMsx3XLV95Jmqc0rcEbCzLZUBxX3daGjshw3Yd3pzEXqM8Mz-58p835VPhSMlZB_yvtP72o2QiKybhq1ob5Ygt7hqlqe08K5StN2rzJJoFkwivhfC3_KDX7XSLEK9PzqPaTOkkJu7y7tJgi6aC6Fq-X3fgeLy20LsBKV_SF4Zd323IZ713iGJFJo0f4mDUXfQmU3wrILk40L\",\"dp\":\"V5SlApD1a0ng5XrzEmOV2EVKVLcS7Z2j1WYLVa4BMxSsi8zJal_v8nllEN8ylDTWKCZt6Dg3fcHtOWKYGe7e2fBMwSsKcjPI2aFVHjI18ZMbC1m6eHWK81zlTQ-ZhgiRMXQvsRCX6Qt8PPvqfV4j-YILYfgJbQ_DRYEfJzq9JCQjFwGUiSoZvOBl9jQMM1u4NmOnvodwf8Jk4Oi1DC65U-CjOC7D07eDPNv74Pog6h6x3u7Z9S-ITvP1NX4h0_ot\",\"dq\":\"rg2IrHzp59w8nZbtd4NDk_stRB8xT6T2pxpH3LhNhEbLrmy0sF4Xemm8frlgRWeEn9dUV2nzveorEJF57bZ3cclEqBGtD72y_IRPZTPgDcYhc9l4xKJkJJuA3yWcQ6eHZHjLAZHi9PszYvFiUx1veHU2S_f7aGKjO14n05wb9r-YUmIKt7AVwK94HksflvNIREa867E2OL1lIJiX3azkyMgTnSHvi0bwgdIGp6uPdDwVW_qvvUmlFinDWflgq58\",\"qi\":\"Dw0f0UwU2KN98stNeuk2UVtG-GyKjCxSSYocGBlShsXzxeLn2faLSkkqhUVicW4o2PmedPDpxanDW2Gl7osamaPb25CodPS3JJxeHWrJ6hGBfKqvJnysZ-0zL8wVzwuNLc3VL-jlCudFfGK03MBapA9h3qjAFFhHZRgLH8y64MimARfh2gLldZ0FgNF9zB4yxmVzvpOng4XRJGzSBKdf9QbuL60Tia2rWR7QU2GtbrXlp4KiNZspZuzmBLZPaa82\",\"p\":\"3lnk-l3lG3ahUzPzhInjwTJEDRrAt0YMhpLmO444TNs0MD__RxRQO8EAhLGCuKaNJmKOg-D5-Fup34KBAcFKw1vCh06PNxoIbHmcY1KwrlA3M_47pK74sK532429sM2N0JH9ti4QjtcD85__THeS6I5g8x8xdSH6sm6ubOiUWUQ96fN10pDv-9D7PuoHGEGlndvsRE0GwWmBIjCnIbxXN4kQsE5YXbrY_WTdPXxwTb8F6Cqc2WXIuhZLlSqd8NjD\",\"q\":\"83zNQcd3dTEpO-j6e7hHeKYghVBhIViD6bXDzD3IX--maTnllGMD-xxHNzOURwl_VzwWo1Al_QQSKDMeEnNDXhSU50qbdhdyVDyRXQuR2Fb9hN5SACX-SiPgfs-2buJVZh8JD-VFSI7ou6eMQ9h-uIGnhoxH3vCs7dJgy-mHGPsvqTypaIo3LHGfM21z8h0yqgyYwaanv5UllaustjvRFId_2oWqNtrn6q410s9W5-6Q6xqkrW2m_lUffu7ViRdT\",\"key_ops\":[\"wrapKey\",\"unwrapKey\",\"encrypt\",\"decrypt\",\"sign\",\"verify\"],\"kid\":\"key_id\"}")
            .put(4096, "{\"kty\":\"RSA\",\"n\":\"mmpRerSZYY4Xx_s89Qn3NMAmJOW0TXtddwjdTedA2CITP_BQW9Q6K7ZEKcAk5W8KwfvAYEIDkWN0iKtoSiBmTJxgCqpDI2MO1D_JJXFP6Tovbtgj1FJ8Ai90w04wmxoCdS9mFC2tE51qUWO7frJpTGrZqVAB8UMH031c3pUPzWedGRvKwj7J-Awtg_IoByaK-qoyRlwfqm8WpHjg6R6Fn1aJY3Fp62l1F3XGayUgqoJmg0_YzYxKpz9WDqIJo15sbyQEpTG6kRybD8T5O8908JU2d3KPp7GOKDNpai5wdaK50QyvaU3BtvKI35IaK367FSVPEZPEoAGgUBCoLXx8N16XHVgjspSMV6NnjBEoehr4xU3nw4cZ-09yZSXJv5FGKmg4pkJGCHQwUfA3XlWNZSPYIgBByyjpMe6gJt_RDBhkkYGVddkwn4HPlMIk3Gi9wzMLuVcLNVeq5k4Us3YsaXdSPI6LSfoosu7mi8qm2JMYlFzbB9_FaxJWqgHlTRSiXX0XNuHPMJoBHtKwh_7VXxiosim8EvszF_Is1ttF77l5lC198slQ5zsZ2XM90Ln9UV04kAyI1jEegDiW37uVSikt-VKyVKSZg5lgmp16CevLnqD2g_YD6fJMmbU4QmeVELhZeQc7Z_XGH7lM1bSeiAJ0dKlDKDURnA0h2LQfquM\",\"e\":\"AQAB\",\"d\":\"D_CvEz5WzGihW9Y7p1qtV5deWKtoaXc1YXcGIWdLR68nfY-OkWw0dRQOWqD92LwVyDX3g02ilfzw_WAfFp0xnPGnmHJAbAQVy83_MwuiIYQNfCEj0bnnbfJS2LaBngFEBQTXl3hU8ulqcuxwtoDZuIxvMQ3pUBaIqvRjWeGEDW2Hch1vA45ScHYRXMWVYZJBAToAkUgr8f7LFOoa4vXGUCSGxOqNnJejrBWkXfsp3BrfVOmGipwo42BOae71lRUc7HwzXo-Q9YSWcpJK3Y8U60umoRNacQfgkkB8aVGnRP1_YRfbeRYQdpT4PDFrh4Hq83aJKwSuD4vGGNMfXqgIdSWhREajFDN653gDIIrt1BPh-snE9HIr0QWJmGQTlKTFfXMEoHx6mxJgUmzZSdQ9BkfCYyInpevh2piUrdpoAEEBapYyTmEHetutMm6cpPd4EK7-yHf3f1k1Rx-HIkiN-pLPiy6x6sRS-272pRNJUjEyRr7QIxgv2rVmQpFd3HZIsmTA9e5HnEH88pbwzqckWeA8nCpe6vr9uJ5MkIMaZq1Exw2bL8TjezbQdRvFm6eO25ECBE_YRuTm72hWfkn1Aocz5_RrSvW6gjwXpgTB6ScNyDrrmo9Kz8DQA7uvRvwhS48fceGGqEt_02qtj_FY1e8Q-XLVsPz-oafqbrTPI0E\",\"dp\":\"D9PGN8qEoUjBFIDKfuilcKwpU25pLnGsgnlzXxORZYB2T1y_DzHVXoSFkcOcFfn2L-AWFSUQmFlt37ULSoSTi2J5KzeydzXfkz4CauzqyEEyv1Uu_FBM6ZDb14ZkYoS8B_vWx7ow99fopwwObs5LH9vtGmiAJczVTNuwUQd_8uRXsWdoy2Ku8XLmNBaxvpXjzbs3ooKIw450PWB1qk455OrQ0k2dqrbY5VlOjgBEk317yCamGbPy2AgC4EnXnAZ0qJ5gN-mJNbjBCjkS0MImMWphJCrXkKMxl1OURKW_ujb7B5EGXcTmJuFxu8uE6_SxDrhmbCfdrwnfVdQXIGyGsw\",\"dq\":\"xEmqiGWKIuuyMX3wElvw2E_qJfuJ58lyAqQOYrM8ROsk4iaV9yc9G57pHsLRdiCYYrYDoisi96LdJ6kScAcS8j5TAuAdHq2riI0MOd-lZr6I4S_3pnjO3SuHYmCoFagnpIo7QM9-l2ZguDrfCjs7PtQZqMWSg-ncHYrHDsbynhe9GPdes01u4XZ3Y2xoYBDJX2iCXVNKJBUeYwlLd01p4eE1O_UkI8GdxQDMOr0ifOjWa9HtmY13Q8yvWoDtA4UX9Ec83mB3F2RWi4b-0C1pxSifCzeo4VZ0uOZ_aR4ZKfx7npWseE6F-Ue2vPx3qnfZQAkXaqJPsR15ZU0ZtJqAEQ\",\"qi\":\"fzpgNvwBpXBjLkqVkKnGD20kTR1lnMfXKJHk_iGE3UY7FVRSGXiUWpRWo-Gh0Lq8jJVXddX3looqv-v_9uKHja2JkHToWSkJajRznFHvz1pMfs7d3Nr4puumuNxJC1rgktnOIK4eikNxxHJ5Rs6TeQOwWxRbmywMiEeUQAwVvgaaF91g8FmNUE4C2BCpav-1fKkr_ydo1j4AgcSaCcKuywPBvp2Fznf4UcND_1vyZVKhSBbqbu6ql8vb9zEo3E8AKsrn9REji0BnA5kHk1Ps7GiQcMSCdazwBq1kw3DYD5Mt4CuyOdg6Btg-MVDXLLS0Dw0VEvcPhi7ypUlpl0RWcQ\",\"p\":\"uFaIjld13k5EkpQtTJ6zw9Zq5QJwaVBu1RiyXTdji8ysU6rxk07HAsUt049BuKqFv2jdKVDsdL25WkIao93hstdl8Kl_7XQX491_1np1hW-NvapNUYo5UUn-SeT4zseoIu8n-GarEAI00U8Xj2M4pewd82zlCQHTCXZiwbWGF2XMfPqvpYhFtEoMyJUf8z6qCvcZgp-neJJNbLgesspHke8--GwYN-QjrUynkmUGZ14BQdnLsmNLeaWY7A92sLSOFYVK8XnXSzOUld2P5JCgTenS5Na5UpuaY1K8od8rci3TE23Gtma5VhqBiPOFgPXmlkpkBe1uRn84iV5avYQfdw\",\"q\":\"1nHaNzR3mE6cyzPqsqNxT_FDDzMXHCmxW8cO_9GzmFBW6MhNArPEIV9BFAo-NRjDKMYKPin67MlyiLCMN-TWTayeNeuvw_WYwKfI_t9xuyf1nsW-TsNoK4n2d0kwoB5OEH9pAtQKv9rSZl4WULePldJBF4lPBhQb0lmRu-HB_SRskZe8CdDcm-gjwLhoP76gstWN5PNzgrPTpxBC6tHdWD-ZbkzIGWzCjxNHAnJkUAEsy3FVllCtO4pMVXz3zupaVzmDlUQ34weWXqkA-C7QgUPNoCD_M7PTNJKQpnTwlgk1Jvn8v4FDrpmYvv5l8B9swMPtlIi9xADuuEg8gO5i9Q\",\"key_ops\":[\"wrapKey\",\"unwrapKey\",\"encrypt\",\"decrypt\",\"sign\",\"verify\"],\"kid\":\"key_id\"}")
            .build();
}
